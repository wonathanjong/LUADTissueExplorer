
import java.awt.image.*;
import java.io.IOException;

import loci.formats.ChannelSeparator;
import loci.formats.FormatException;
import loci.formats.FormatTools;
import loci.formats.ImageReader;
import loci.formats.in.*;
import loci.common.*;
import loci.common.DataTools;

import java.awt.Color;
import java.awt.color.*;



public class DecodeSVS {
    
    public static void main(String[] args) throws IOException, FormatException {
        
        ImageReader b = new ImageReader();
        b.setId("/Users/jilkowahng/Desktop/test.svs");
        b.setSeries(1);
        ChannelSeparator r = new ChannelSeparator(b);
        
        int optWidth =
        r.getOptimalTileWidth();
        while(r.getSizeX()%optWidth != 0)
        {
            optWidth++;
        }
        
        int optHeight =
        r.getOptimalTileHeight();
        while(r.getSizeY()%optHeight != 0)
        {
            optHeight++;
        }
        
        int matrixWidth = r.getSizeX()/r.getOptimalTileWidth();
        int matrixHeight = r.getSizeY()/r.getOptimalTileHeight();
        int byteNum = r.openBytes(0,0,0,optWidth,optHeight).length;
        
        byte[][][] byteData = new byte[matrixWidth][matrixHeight][byteNum];
        int[][] rgbData = new int[r.getSizeX()][r.getSizeY()];
        int pixelType = r.getPixelType();
        boolean floatingPoint = (pixelType == FormatTools.FLOAT || pixelType == FormatTools.DOUBLE);
        int bytesPerPixel = FormatTools.getBytesPerPixel(pixelType);
        boolean litteEndian = r.isLittleEndian();
        
        System.out.println("Converting svs to matrix and performing pre-processing");
        
        for (int x= 0, countX =0; x< r.getSizeX(); x+= optWidth, countX++){
            for(int y= 0, countY= 0; y<r.getSizeY(); y+= optHeight, countY++){
                
                byteData[countX][countY] = r.openBytes(0,x,y,optWidth,optHeight);
                byte[][] dat = (byte[][]) DataTools.makeDataArray2D(byteData[countX][countY], bytesPerPixel, floatingPoint, litteEndian, optHeight);
                System.out.println(optWidth + " width "+ optHeight);
                System.out.println("getSizeX: "+ r.getSizeX() + " getSizeY: "+ r.getSizeY() + " datX: "+ dat.length + " datY: " + dat[0].length);
                System.out.println(dat[0][0]);
                for(int datX =0; datX<dat.length; datX++ ){
                    for(int datY=0; datY<dat[0].length; datY++){
                        int rgb = (int)dat[datX][datY];
                        int red = (rgb >> 16) & 0xFF;
                        int green = (rgb >> 8) & 0xFF;
                        int blue = rgb & 0xFF;
                        float[] hsv = new float[3];
                        Color.RGBtoHSB(red,green,blue,hsv);
                        //System.out.println("x: "+x + " y: "+y+" datX: "+datX+" datY: "+datY);
                        float h = hsv[0];
                        float s = hsv[1];
                        float v = hsv[2];
                        if(v>=0.1 && s<=.1)
                        {
                            rgbData[datX+(countX*optWidth)][datY+(countY*optHeight)] = 255;
                        }
                        else if((h>0.4&&h<.7&&s>.1)||v<.1){
                            rgbData[datX+(countX*optWidth)][datY+(countY*optHeight)] = 255;
                        }
                        else if(((s-v)>-.25)&&((s+v)>1.3)){
                            rgbData[datX+(countX*optWidth)][datY+(countY*optHeight)] = 255; 
                        }
                        else{
                            rgbData[datX+(countX*optWidth)][datY+(countY*optHeight)] = ((red&0x0ff)<<16)|((green&0x0ff)<<8)|(blue&0x0ff);
                        }
                    }
                }
            }
        }
        System.out.println("complete");       
    }
    
}
