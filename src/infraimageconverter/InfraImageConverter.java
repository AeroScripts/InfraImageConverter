/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package infraimageconverter;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.ConcurrentLinkedDeque;
import javax.imageio.ImageIO;
import javax.imageio.spi.IIORegistry;

/**
 *
 * @author Aero
 */
public class InfraImageConverter {

    /**
     * @param args the command line arguments
     */
    public static BufferedImage ren(Image im){
        BufferedImage ret = new BufferedImage(im.getWidth(null), im.getHeight(null), BufferedImage.TYPE_INT_RGB);
        ret.createGraphics().drawImage(im, 0,0,null);
        return ret;
    }
    public static void main(String[] args) {
        int cores = Integer.parseInt(args[0]);
        String targetFormat = args[1];
        System.out.println("THIS PROGRAM WAS MADE BY AERO FOR INFRA");
        System.out.println("Do not give it to anyone who is pro changes, or a monkey");
        System.out.println("#nochanges");
        System.out.println("This will convert all .tga images into ." + targetFormat + " and put them inside the \"" + targetFormat + "\" folder");
        System.out.println();
        new File("./" + targetFormat + "/").mkdirs();
//        System.out.println("Loading TGA reader library...");
//        IIORegistry registry = IIORegistry.getDefaultInstance();
//        registry.registerServiceProvider(new com.realityinteractive.imageio.tga.TGAImageReaderSpi());

        ConcurrentLinkedDeque<File> queue = new ConcurrentLinkedDeque<File>();

        System.out.println("Scanning directory for .TGA files...");
        for (File f : new File("./").listFiles()) {
            if (f.getName().toLowerCase().endsWith("tga")) {
                try {
                    if (!new File("./" + targetFormat + "/" + f.getName().substring(0, f.getName().length() - 3) + targetFormat).exists()) {
                        queue.offer(f);
                    }
                } catch (Throwable t) {
                    System.out.println("ERROR! BLAME AERO! SCREENSHOT THIS!");
                    t.printStackTrace();
                }
            }
        }
        System.out.println("Starting " + cores + " threads to convert " + queue.size() + " images...");
        for (int i = 0; i < cores; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (!queue.isEmpty()) {
                        File f = queue.poll();
                        if (f != null) {
                            try {
                                System.out.println("Reading " + f.getName() + "...");
                                long time = System.currentTimeMillis();
                                BufferedImage image = (FastTarga_lowmem.getImage(f.toString()));/*ImageIO.read(f)*/;
//                                System.out.println("Loaded targa in " + (System.currentTimeMillis() - time) + "ms");
                                System.out.println("Writing " + f.getName().substring(0, f.getName().length() - 3) + targetFormat + "...");
                                ImageIO.write(image, targetFormat, new File("./" + targetFormat + "/" + f.getName().substring(0, f.getName().length() - 3) + targetFormat + ".temp"));
                                new File("./" + targetFormat + "/" + f.getName().substring(0, f.getName().length() - 3) + targetFormat + ".temp").renameTo(new File("./" + targetFormat + "/" + f.getName().substring(0, f.getName().length() - 3) + targetFormat));
                            } catch (Throwable t) {
                                System.out.println("ERROR! BLAME AERO! SCREENSHOT THIS!");
                                t.printStackTrace();
                            }
                        }
                    }
                }
            }).start();
        }
    }

}
