package lib.turbok.swing.common;

import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;



/**
 * 이미지를 Clipboard를 이용하여 전달하기 위한 클래스
 */
public class TransferableImage implements Transferable
{
    private Image   _image;
    
    public TransferableImage(Image image)
    {
        this._image = image;
    }
    
    @Override
    public Object getTransferData(DataFlavor flavor)
            throws UnsupportedFlavorException, IOException
    {
        if( flavor.equals(DataFlavor.imageFlavor) && _image != null )
        {
            return _image;
        }
        else
        {
            throw new UnsupportedFlavorException(flavor);
        }
    }
    
    @Override
    public DataFlavor[] getTransferDataFlavors()
    {
        DataFlavor[] flavors = new DataFlavor[1];
        flavors[0] = DataFlavor.imageFlavor;
        return flavors;
    }
    
    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor)
    {
        DataFlavor[] flavors = getTransferDataFlavors();
        for(int _image = 0; _image < flavors.length; _image++)
        {
            if( flavor.equals(flavors[_image]) )
            {
                return true;
            }
        }
        
        return false;
    }
}
