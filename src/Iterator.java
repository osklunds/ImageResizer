
import java.util.*;
import java.util.concurrent.*;
import java.io.*;
import java.nio.file.*;
import org.apache.commons.io.*;
import java.time.*;
import net.coobird.thumbnailator.*;
import net.coobird.thumbnailator.resizers.configurations.*;

import com.drew.metadata.*;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.imaging.jpeg.JpegSegmentMetadataReader;
import com.drew.metadata.exif.*;
import com.drew.metadata.iptc.IptcReader;

/**
 * The main class. It iterates recursively through the folder hierarchy.
 */
public class Iterator
{
  public static void main(String[] args)
  {
    if (args.length != 5)
    {
      System.out.println("Wrong number of arguments.");
      System.out.println("1: Source path");
      System.out.println("2: Destination path");
      System.out.println("3: 1 iff should print create/delete messages, else 0");
      System.out.println("4: 1 iff should print entry/exit messages, else 0");
      System.out.println("5: M iff mobile, T iff TV");
      return;
    }
    
    String src = args[0];
    String dst = args[1];
    boolean cdMess = false;
    boolean eeMess = false;
    boolean mode = false;
    
    try
    {
      if (Integer.parseInt(args[2]) == 1)
      {
        cdMess = true;
      }
      else if (Integer.parseInt(args[2]) != 0)
      {
        System.out.println("Argument error");
        return;
      }
      
      if (Integer.parseInt(args[3]) == 1)
      {
        eeMess = true;
      }
      else if (Integer.parseInt(args[3]) != 0)
      {
        System.out.println("Argument error");
        return;
      }
      
      if (args[4].equals("M"))
      {
        mode = true;
      }
      else if (!args[4].equals("T"))
      {
        System.out.println("Argument error");
        return;
      }
    }
    catch (Exception e)
    {
      System.out.println("Argument error");
      System.exit(0);
    }
    
    // The iterator object that will be used
    Iterator iterator = new Iterator();
                                
    iterator.printCreateDelete = cdMess;
    iterator.printEntryExit = eeMess;
    iterator.mode = mode;
    
    long t0 = System.nanoTime();
    iterator.compute(new File(src),new File(dst));
    long t1 = System.nanoTime();
    
    System.out.println("Time: " + ((t1 - t0) / 1000000000) + " seconds");
  }
  
  /**
   * To print or not print delete and create messages for individual
   * files and folders.
   */
  static boolean printCreateDelete = false;
  
  /**
   * To print or not print entry/exit messages for folders
   */
  static boolean printEntryExit = false;
  
  /**
   * Mobile or TV mode. True means Mobile, False mean TV.
   * Mobile: only images. Resolution 1024x1024. 0.3x compression.
   * TV: images and videos. Resolution 1920x1080. 0.8x compression.
   */
  public static boolean mode = true;
  
  /**
   * Syncronizes the source and destination folder by resizing images
   * into the destination. Continues recursively for subfolders.
   */
  public void compute(File srcFolder, File dstFolder)
  {
    printEntry(srcFolder, dstFolder);
  
    File[] srcFiles = srcFolder.listFiles();
    File[] dstFiles = dstFolder.listFiles();
    
    // Set of "items", which store name without date/extension, extension
    // and a file object
    HashSet<Item> src = new HashSet<>(srcFiles.length);
    HashSet<Item> dst = new HashSet<>(dstFiles.length);
    
    // 1. Generate source items
    for (File srcFile : srcFiles)
    {
      String srcName = srcFile.getName();
      String ext = getExtension(srcName);
      
      src.add(new Item(srcFile, srcName, ext));
    }
    
    // 2. Generate destination items
    for (File dstFile : dstFiles)
    {
      String dstName = dstFile.getName();
             dstName = removeDate(dstName);
             dstName = removeExtension(dstName);
      String ext = getExtension(dstName);
      
      dst.add(new Item(dstFile, dstName, ext));
    }
    
    // 3. Iterate over all items in destination
    for (Item dstItem : dst)
    {
      if (!src.contains(dstItem) && dstItem.file.isDirectory())
      {
        printDeleteFolder(dstFolder, dstItem.name);
        try
        {
          FileUtils.deleteDirectory(dstItem.file);
        }
        catch (Exception e)
        {
          System.out.println("Delete dir failed: " + e);
        }
      }
    
      if ((!src.contains(dstItem) || !shouldKeep(dstItem.ext)) &&
          (dstItem.file.isFile()))
      {
        printDeleteFile(dstFolder, dstItem.name);
        dstItem.file.delete();
      }
    }
    
    // 4. Iterate over all items in source
    for (Item srcItem : src)
    {
      // An folder was encountered in the source
      if (srcItem.file.isDirectory())
      {
        File dstFile = new File(dstFolder, srcItem.name);
        
        if (!dst.contains(srcItem))
        {
          printCreateFolder(dstFolder, srcItem.name);
          dstFile.mkdir();
        }

        // Continue recursively
        compute(srcItem.file, dstFile);
      }
      
      // A file is missing from destination
      if (!dst.contains(srcItem) && shouldKeep(srcItem.ext))
      {
        if (shouldTransform(srcItem.ext))
        {
          printCreateFileTransform(dstFolder, srcItem.name);
          ImageTransform tf = new ImageTransform(srcItem.file, 
                                                 srcItem.name,
                                                 dstFolder);
          tf.compute();
        }
        
        if (shouldCopy(srcItem.ext))
        {
          printCreateFileCopy(dstFolder, srcItem.name);
          File dstFile = new File(dstFolder, srcItem.name + "." + srcItem.ext);
          try
          {
            Files.copy(srcItem.file.toPath(), dstFile.toPath());
          }
          catch (Exception e)
          {
            System.out.println("Copy failed: " + e);
          }
        }
      }
    }
    
    printExit(srcFolder, dstFolder);  
  }
  
  /**
   * Prints that entered source folder, but only if static var says so.
   */
  private void printEntry(File srcFolder, File dstFolder)
  {
    if (printEntryExit)
    {
      System.out.println("Entering \"" + srcFolder + "\"");
    }
  }
  
  /**
   * Prints that exited source folder, but only if static var says so.
   */
  private void printExit(File srcFolder, File dstFolder)
  {
    if (printEntryExit)
    {
      System.out.println("Exiting \"" + srcFolder + "\"");
    }
  }
  
  /**
   * Prints info that folder was deleted, but only if static variable says so.
   * 
   * @param name name of the folder in destination to be deleted.
   */
  private void printDeleteFolder(File dstFolder, String name)
  {
    if (printCreateDelete)
    {
      System.out.println("Deleting folder \"" + name + "\" in \"" +
                          dstFolder + "\"");
    }
  }
  
  /**
   * Prints info that file was deleted, but only if static variable says so.
   * 
   * @param name name of the file in destination to be deleted.
   */
  private void printDeleteFile(File dstFolder, String name)
  {
    if (printCreateDelete)
    {
      System.out.println("Deleting file \"" + name + "\" in \"" +
                          dstFolder + "\"");
    }
  }
  
  /**
   * Prints info that a folder was created, but only if static variable says so.
   * 
   * @param name name of the folder in destination to be created.
   */
  private void printCreateFolder(File dstFolder, String name)
  {
    if (printCreateDelete)
    {
      System.out.println("Creating folder \"" + name + "\" in \"" +
                          dstFolder + "\"");
    }
  }
  
  /**
   * Prints info that an image was transformed and created,
   * but only if static variable says so.
   * 
   * @param name name of the file in destination to be created.
   */
  private void printCreateFileTransform(File dstFolder, String name)
  {
    if (printCreateDelete)
    {
      System.out.println("Creating transformed file \"" + name + "\" in \"" +
                          dstFolder + "\"");
    }
  }
  
  /**
   * Prints info that a file was copied and created,
   * but only if static variable says so.
   * 
   * @param name name of the file in destination to be created.
   */
  private void printCreateFileCopy(File dstFolder, String name)
  {
    if (printCreateDelete)
    {
      System.out.println("Creating copied file \"" + name + "\" in \"" +
                          dstFolder + "\"");
    }
  }
  
  /**
   * Removes a date of format "   yyyy-mm-dd hh;mm   " from the front of the 
   * file name.
   * If there is no date, the name is unmodified.
   * 
   * @param name the name of the file.
   * @return the name of the file without the date.
   */
  private String removeDate(String name)
  {
    if (name.length() >= 22 &&
        name.charAt(0) == ' ' &&
        name.charAt(1) == ' ' &&
        name.charAt(2) == ' ')
    {
      return name.substring(22);
    }
    else
    {
      return name;
    }
  }
  
  /**
   * Gets the file extension of a file.
   * 
   * @param file name of the file.
   * @return the extension without dot.
   */
  private String getExtension(String file)
  {
    // No extension
    if (file.indexOf(".") == -1)
    {
      return "";
    }
  
    try 
    {
      return file.substring(file.lastIndexOf(".") + 1);
    } 
    catch (Exception e) 
    {
      return "";
    }
  }
  
  /**
   * Removes the file extension of a file.
   * 
   * @param file name of the file.
   * @return the name without the extension.
   */
  private String removeExtension(String file)
  {
    try 
    {
      return file.substring(0, file.lastIndexOf("."));
    } 
    catch (Exception e) 
    {
      return file;
    }
  }
  
  /**
   * Determines if an extension should be transformed from source to 
   * destination. A transformation is rescale, compress and add date.
   * I.e. image of format jpg, png
   * 
   * @param ext the extension of the file
   * @return true iff should be resized.
   */
  private boolean shouldTransform(String ext)
  {
    if (ext.equals("JPG") ||
        ext.equals("jpg") ||
        ext.equals("JPEG") ||
        ext.equals("jpeg") ||
        ext.equals("PNG") ||
        ext.equals("png"))
    {
      return true;
    }
    else
    {
      return false;
    }
  }
  
  /**
   * Determines if a file should be copied from source to destination.
   * I.e. movie of format m4v, mov, avi, mpeg
   * 
   * @param ext the extension of the file
   * @return true iff should be copied.
   */
  private boolean shouldCopy(String ext)
  {
    // Mobile, skip movies.
    if (mode)
    {
      return false;
    }
  
    if (ext.equals("MOV") ||
        ext.equals("mov") ||
        ext.equals("AVI") ||
        ext.equals("avi") ||
        ext.equals("MPG") ||
        ext.equals("mpg") ||
        ext.equals("MPEG") ||
        ext.equals("mpeg") ||
        ext.equals("M4V") ||
        ext.equals("m4v") ||
        ext.equals("MP4") ||
        ext.equals("mp4"))
    {
      return true;
    }
    else
    {
      return false;
    }
  }
  
  /**
   * Determines if a file should be kept in the destination
   * I.e. image or movie from the above methods.
   * 
   * @param ext the extension of the file
   * @return true iff should be kept.
   */
  private boolean shouldKeep(String ext)
  {
    return shouldTransform(ext) || shouldCopy(ext);
  }
}