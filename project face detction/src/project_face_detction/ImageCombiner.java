package project_face_detction;



import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.io.IOException;
import javax.imageio.ImageIO;


public class ImageCombiner extends WindowAdapter {
    Image resultImage;
    Image img1;
    Image img2;
    Image imgr;
    ImageDisplay ic1;
    ImageDisplay ic2;
    ImageDisplay icr;
    int ccnt;

    public class ImageDisplay extends Canvas {
        private Image i;
        private Frame myframe;

        public ImageDisplay(Image img, boolean makeframe) {
            i = img;
            if (makeframe) {
                myframe = new Frame("Image");
                myframe.add(this, BorderLayout.CENTER);
                MediaTracker mt = new MediaTracker(this);
                mt.addImage(i, 1);
                try { mt.waitForAll(); } catch (Exception ie) { }
                setSize(i.getWidth(this), i.getHeight(this));
                myframe.pack();
            }
            else {
                myframe = null;
                setSize(i.getWidth(this), i.getHeight(this));
            }
        }
        public void paint(Graphics g) {
            g.drawImage(i, 0, 0, this);
        }

        public Frame getFrame() { return myframe; }

        public void placeAndShow(int x, int y) {
            if (myframe != null) {
                myframe.setLocation(x,y);
                myframe.show();
            }
        }
    }

   
    public class ImageMerger
    {
        protected double w1, w2;
        protected Image i1;
        protected Image i2;
        protected ColorModel cm;

        int rwid, rhgt;
        protected int results[];

        public ImageMerger(Image img1, Image img2) {
            cm = null;
            i1 = img1;
            i2 = img2;
            w1 = 0.5;
            w2 = 0.5;
            rwid = 0;
            rhgt = 0;
            results = null;
        }

       
        public void setWeights(double img1weight, double img2weight) {
            w1 = img1weight;
            w2 = img2weight;
        }

        
        public boolean generate(Component comp) {
            MediaTracker mt;
            mt = new MediaTracker(comp);
            mt.addImage(i1, 1);
            mt.addImage(i2, 2);
            try { mt.waitForAll(); } catch (Exception ie) { }

            int wid1, wid2;
            int hgt1, hgt2;

            wid1 = i1.getWidth(comp);
            wid2 = i2.getWidth(comp);
            hgt1 = i1.getHeight(comp);
            hgt2 = i2.getHeight(comp);

            rwid = Math.max(wid1, wid2);
            rhgt = Math.max(hgt1, hgt2);

            results = new int[rwid * rhgt];

            int [] p1 = new int[rwid * rhgt];
            int [] p2 = new int[rwid * rhgt];

            PixelGrabber pg1 = new PixelGrabber(i1, 0, 0, wid1, hgt1, p1, 0, rwid);
            try { pg1.grabPixels(); } catch (Exception ie1) { }

            PixelGrabber pg2 = new PixelGrabber(i2, 0, 0, wid2, hgt2, p2, 0, rwid);
            try { pg2.grabPixels(); } catch (Exception ie2) { }

            cm = ColorModel.getRGBdefault();

            int y, x, rp, rpi;
            int red1, red2, redr;
            int green1, green2, greenr;
            int blue1, blue2, bluer;
            int alpha1, alpha2, alphar;
            double wgt1, wgt2;

            for(y = 0; y < rhgt; y++) {
                for(x = 0; x < rwid; x++) {
                    rpi = y * rwid + x;
                    rp = 0;
                    blue1 = p1[rpi] & 0x00ff;
                    blue2 = p2[rpi] & 0x00ff;
                    green1 = (p1[rpi] >> 8) & 0x00ff;
                    green2 = (p2[rpi] >> 8) & 0x00ff;
                    red1 = (p1[rpi] >> 16) & 0x00ff;
                    red2 = (p2[rpi] >> 16) & 0x00ff;
                    alpha1 = (p1[rpi] >> 24) & 0x00ff;
                    alpha2 = (p2[rpi] >> 24) & 0x00ff;

                    
                    wgt1 = w1 * (alpha1 / 255.0);
                    wgt2 = w2 * (alpha2 / 255.0);
                    redr = (int)(red1 * wgt1 + red2 * wgt2);
          redr = (redr < 0)?(0):((redr>255)?(255):(redr));
                    greenr = (int)(green1 * wgt1 + green2 * wgt2);
          greenr = (greenr < 0)?(0):((greenr>255)?(255):(greenr));
                    bluer = (int)(blue1 * wgt1 + blue2 * wgt2);
          bluer =  (bluer < 0)?(0):((bluer>255)?(255):(bluer));
          alphar = 255;

                    rp = (((((alphar << 8) + (redr & 0x0ff)) << 8) + (greenr & 0x0ff)) << 8) + (bluer & 0x0ff);

                   
                    results[rpi] = rp;
                }
            }
            return true;
        }

       
        public Image getGeneratedImage() {
            Image ret;
            MemoryImageSource mis;
            if (results == null) {
                Frame dummy = new Frame();
                generate(dummy);
                dummy.dispose();
            }
            mis = new MemoryImageSource(rwid, rhgt, cm, results, 0, rwid);
            ret = Toolkit.getDefaultToolkit().createImage(mis);
            return ret;
        }

       
        public void dispose() {
            results = null;
            return;
        }

    }


    public ImageCombiner(Image i1, double w1, Image i2, double w2) {
        ccnt = 0;
        img1 = i1;
        img2 = i2;

        ic1 = new ImageDisplay(img1, true);
        ic1.getFrame().addWindowListener(this);
        ic1.placeAndShow(50,50);

        ic2 = new ImageDisplay(img2, true);
        ic2.getFrame().addWindowListener(this);
        ic2.placeAndShow(100,90);

        ImageMerger imerge = new ImageMerger(i1, i2);
        imerge.setWeights(w1, w2);
        imerge.generate(ic2);
        imgr = imerge.getGeneratedImage();
        imerge.dispose();

        icr = new ImageDisplay(imgr, true);
        icr.getFrame().addWindowListener(this);
        icr.placeAndShow(200,180);
    }

    public void windowClosing(WindowEvent we) {
        we.getWindow().hide();
        we.getWindow().dispose();
        ccnt++;
        if (ccnt >= 3) System.exit(0);
    }

    public static void main(String [] args) throws IOException {
    
   String imgf1 = null, imgf2 = null;
   double w1 = 0.5;
   double w2 = 0.5;
 
 BufferedImage i1 = ImageIO.read(ImageCombiner.class.getResourceAsStream("imgf1.jpg"));
    BufferedImage i2 = ImageIO.read(ImageCombiner.class.getResourceAsStream("imgf2.jpg"));
  
        ImageCombiner icom = new ImageCombiner(i1, w1, i2, w2);
    }
}

