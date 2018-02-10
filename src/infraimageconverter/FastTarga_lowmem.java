/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package infraimageconverter;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 *
 * @author Aero
 */
class FastTarga_lowmem {
    
    public static BufferedImage getImage(String fileName) throws IOException {
        File f = new File(fileName);
//        byte[] buf = new byte[(int) f.length()];
        BufferedInputStream bis = (new BufferedInputStream(new FileInputStream(f)));
//        bis.readFully(buf);
//        bis.close();
        return decode(bis);
    }

    private static int offset;

    private static int btoi(byte b) {
        int a = b;
        return (a < 0 ? 256 + a : a);
    }

    private static int read(byte[] buf) {
        return btoi(buf[offset++]);
    }

    public static BufferedImage decode(BufferedInputStream in) throws IOException {
        offset = 0;

        // Reading header bytes
        // buf_2=image type code 0x02=uncompressed BGR or BGRA
        // buf[12]+[13]=width
        // buf[14]+[15]=height
        // buf_16=image pixel size 0x20=32bit, 0x18=24bit
        // buf{17]=Image Descriptor Byte=0x28 (00101000)=32bit/origin 
        //         upperleft/non-interleaved
        int buf_2 = 0;
        int buf_16 = 0;
        for (int i = 0; i < 12; i++){
            if(i == 2)
                buf_2 = in.read();
            else
                in.read();
        }
        int width = in.read() + (in.read() << 8);   // 00,04=1024
        int height = in.read() + (in.read() << 8);  // 40,02=576
        BufferedImage bimg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        
        buf_16 = in.read();
        in.read();

        int n = width * height;
        int[] pixels = new int[n];
        int idx = 0;

        if (buf_2 == 0x02 && buf_16 == 0x20) { // uncompressed BGRA
            while (n > 0) {
                int b = in.read();
                int g = in.read();
                int r = in.read();
                int a = in.read();
                int v = (a << 24) | (r << 16) | (g << 8) | b;
                pixels[idx++] = v;
                n -= 1;
            }
        } else if (buf_2 == 0x02 && buf_16 == 0x18) {  // uncompressed BGR
            while (n > 0) {
                int b = in.read();
                int g = in.read();
                int r = in.read();
                int a = 255; // opaque pixel
                int v = (a << 24) | (r << 16) | (g << 8) | b;
                pixels[idx++] = v;
                n -= 1;
            }
        } else {
            // RLE compressed
            while (n > 0) {
                int nb = in.read(); // num of pixels
                if ((nb & 0x80) == 0) { // 0x80=dec 128, bits 10000000
                    for (int i = 0; i <= nb; i++) {
                        int b = in.read();
                        int g = in.read();
                        int r = in.read();
                        pixels[idx++] = 0xff000000 | (r << 16) | (g << 8) | b;
                    }
                } else {
                    nb &= 0x7f;
                    int b = in.read();
                    int g = in.read();
                    int r = in.read();
                    int v = 0xff000000 | (r << 16) | (g << 8) | b;
                    for (int i = 0; i <= nb; i++)
                        pixels[idx++] = v;
                }
                n -= nb + 1;
            }
        }

        
        bimg.setRGB(0, 0, width,height, pixels, 0,width);
        return bimg;
    }
}