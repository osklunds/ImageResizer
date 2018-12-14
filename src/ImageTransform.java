
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
  A class representing an image that will be transformed.
 */
public class ImageTransform
{
  /**
   * Returns which width an image should have. Decided by the mode.
   *
   * @return width.
   */
  private static int imageWidth()
  {
    return Iterator.mode ? 1024 : 1920;
  }
  
  /**
   * Returns which height an image should have. Decided by the mode.
   * 
   * @return height.
   */
  private static int imageHeight()
  {
    return Iterator.mode ? 1024 : 1080;
  }
  
  /**
   * Returns which output quality should be used. Decided by mode.
   * 
   * @return outputQuality.
   */
  private static double outputQuality()
  {
    return Iterator.mode ? 0.3 : 0.8;
  }

  /**
   * File for the original image.
   */
  private File srcFile;
  
  /**
   * Name of the file.
   */
  private String name;
  
  /**
   * Folder where to place the transformed file.
   */
  private File dstFolder;
  
  public ImageTransform(File srcFile, String name, File dstFolder)
  {
    this.srcFile = srcFile;
    this.name = name;
    this.dstFolder = dstFolder;
  }
  
  /**
   * Creates the destination file. It will be in the destination folder, with the source file
   * name with the date prepended.
   */
  public void compute()
  {
    try
    {
      File dstFile = new File(dstFolder, dateFromFile(srcFile, name) + ".jpg");
      Thumbnails.of(srcFile).size(imageWidth(), imageHeight()).outputFormat("JPEG").
                 outputQuality(outputQuality()).rendering(Rendering.QUALITY).toFile(dstFile);  
    }
    catch (Exception e)
    {
      System.out.println("Error writing file: " + e);
    }
  }
  
  /**
   * Prepends a date of format "   yyyy-mm-dd hh;mm   ".
   * The date is based in the file's exif information.
   * If no exif is available, just the file name is returned.
   * 
   * @param srcFile file object representing the image.
   * @param name name of the file without extension.
   * @return the new name.
   */
  private String dateFromFile(File srcFile, String name)
  {
    String dateString = null;
  
    try
    {
      Metadata metadata = ImageMetadataReader.readMetadata(srcFile);
    
      // obtain the Exif SubIFD directory
      ExifSubIFDDirectory directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
  
      // query the datetime tag's value
      Date date = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
      
      LocalDateTime ldt = LocalDateTime.ofInstant(date.toInstant(), ZoneId.of("GMT"));
      int year  = ldt.getYear();
      int month = ldt.getMonthValue();
      int day   = ldt.getDayOfMonth();
      int hour  = ldt.getHour();
      int minute = ldt.getMinute();

      dateString = String.format("   %d-%02d-%02d %02d;%02d   ", year, month, day, hour, minute);
    }
    catch (Exception e)
    {
      // Happens a lot
      //System.out.println("Metadata error " + e);
    }
    
    if (dateString != null)
    {
      return dateString + name;
    }
    else
    {
      return name;
    }
  }
}