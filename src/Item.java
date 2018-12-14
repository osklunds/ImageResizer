
import java.io.*;

/**
 * Class for storing a file object, a file name and an extension.
 * For comparsion, only the file name is considered.
 * Used by Iterator when listing the files of a folder.
 */
public class Item
{
  public File file;
  public String name;
  public String ext;
  
  public Item(File file, String name, String ext)
  {
    this.file = file;
    this.name = name;
    this.ext = ext;
  }
  
  @Override
  public boolean equals(Object o)
  {
    Item i = (Item)o;
    
    return name.equals(i.name);
  }
  
  @Override
  public int hashCode()
  {
    return name.hashCode();
  }
}