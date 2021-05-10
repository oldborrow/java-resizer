package mts.teta.resizer;

import marvin.image.MarvinImage;
import marvin.io.MarvinImageIO;
import marvinplugins.MarvinPluginCollection;
import mts.teta.resizer.imageprocessor.BadAttributesException;
import net.coobird.thumbnailator.Thumbnails;
import picocli.CommandLine;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "resizer", mixinStandardHelpOptions = true, version = "resizer 0.0.1", description = "my resizer app")
public class ResizerApp /*extends ConsoleAttributes*/ implements Callable<Integer> {

    private File inputFile;
    private File outputFile;
    private Integer width;
    private Integer height;
    private Integer quality = 10000;
    private String format = "jpg";

    public static void main(String... args) {
        int exitCode = runConsole(args);
        System.exit(exitCode);
    }

    void setInputFile(File file) {
        System.out.println(file);
        inputFile = file;
    }

    void setOutputFile(File file) {
        outputFile = file;
    }

    void setResizeWidth(Integer reducedPreviewWidth) {
        width = reducedPreviewWidth;
    }

    void setResizeHeight(Integer reducedPreviewHeight) {
        height = reducedPreviewHeight;
    }

    void setQuality(Integer i) {
        quality = i;
    }

    void Crop(File input, File output, Integer x, Integer y, Integer width, Integer height) {
        MarvinImage image = MarvinImageIO.loadImage(input.getPath());
        MarvinPluginCollection.crop(image.clone(), image, x, y, width, height);
        MarvinImageIO.saveImage(image, output.getPath());
    }

    void GaussianBlur(File input, File output, int radius) {
        MarvinImage image = MarvinImageIO.loadImage(input.getPath());
        MarvinPluginCollection.gaussianBlur(image.clone(), image, radius);
        MarvinImageIO.saveImage(image, output.getPath());
    }

    protected static int runConsole(String[] args) {
        return new CommandLine(new ResizerApp()).execute(args);
    }

    /*
    @CommandLine.Parameters(paramLabel = "IMAGE", description = "input image")
    String[] absolutePathInputAndOutput;

    @CommandLine.Option(names = "--resize", paramLabel = "SIZE", description = "resize the image")
    int[] dims;

    @CommandLine.Option(names = "--quality", paramLabel = "QUALITY", description = "JPEG/PNG compression level")
    int quality;

    @CommandLine.Option(names = "--crop", paramLabel = "CROP", description = "cut out one rectangular area of the image")
    int[] crop;

    @CommandLine.Option(names = "--blur", paramLabel = "BLUR", description = "reduce image noise detail levels")
    int blur;
    */


    @Override
    public Integer call() throws Exception {
        if (quality <= 0) {
            throw new BadAttributesException("Please check params!");
        }
        try {
            Thumbnails.of(inputFile)
                    .size(width, height)
                    .outputQuality(quality * 0.01)
                    .keepAspectRatio(false)
                    .outputFormat(format)
                    .toFile(outputFile);
        } catch(Exception e) {
            throw new IIOException("Can't read input file!");
        }
        //ImageProcessor imageProcessor = new ImageProcessor();
        //imageProcessor.processImage(ImageIO.read(inputFile), this);
        return 0;
    }
}
