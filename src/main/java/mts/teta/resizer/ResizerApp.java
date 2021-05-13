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
import java.io.*;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "resizer", mixinStandardHelpOptions = true, version = "resizer 0.0.1", description = "Version: 0.0.1\n" +
        "Available formats: jpeg png")
public class ResizerApp /*extends ConsoleAttributes*/ implements Callable<Integer> {
    private Integer width;
    private Integer height;
    private File inputFile;
    private File outputFile;
    private float quality = 10000;
    private String format = "jpg";

    @CommandLine.Parameters(paramLabel = "input-image", description = "input image")
    String InputPathConsole;

    @CommandLine.Option(names = "--resize", arity = "2", paramLabel = "width height", description = "resize the image")
    int[] dimsConsole;

    @CommandLine.Option(names = "--quality", paramLabel = "value", description = "JPEG/PNG compression level")
    private Integer qualityConsole = 10000;

    @CommandLine.Option(names = "--crop", arity = "4", paramLabel = "width height x y", description = "cut out one rectangular area of the image")
    int[] cropConsole;

    @CommandLine.Option(names = "--blur", paramLabel = "radius", description = "reduce image noise detail levels")
    int blurConsole;

    @CommandLine.Option(names = "--format", paramLabel = "'Output format'", description = "the image format type")
    String formatConsole;

    @CommandLine.Parameters(paramLabel = "output-image", description = "output image")
    String OutputPathConsole;

    void setInputFile(File file) {
        inputFile = file;
    }

    public static void main(String... args) {
        int exitCode = runConsole(args);
        System.exit(exitCode);
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
        return new picocli.CommandLine(new ResizerApp()).execute(args);
    }

    private static void copyFileUsingStream(File source, File dest) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } finally {
            is.close();
            os.close();
        }
    }

    protected int[] getWidthAndHeight(File file) throws IOException {
        BufferedImage bimg = ImageIO.read(file);
        int width = bimg.getWidth();
        int height = bimg.getHeight();
        return new int[]{width, height};
    }

    @Override
    public Integer call() throws Exception {
        if (InputPathConsole != null) {
            File inputF = new File(InputPathConsole);
            File answer = new File(OutputPathConsole);
            copyFileUsingStream(inputF, answer);
            if (dimsConsole != null) {
                try {
                    Thumbnails.of(answer)
                            .size(dimsConsole[0], dimsConsole[1])
                            .keepAspectRatio(false)
                            .toFile(answer);
                } catch (Exception e) {
                    throw new IIOException("Can't read input file!");
                }
            }
            if (qualityConsole != 10000) {
                if (qualityConsole < 0 || qualityConsole > 100) {
                    throw new BadAttributesException("Please check params!");
                }
                int[] dims = getWidthAndHeight(answer);
                Thumbnails.of(answer)
                        .size(dims[0], dims[1])
                        .outputQuality(qualityConsole * 0.01)
                        .toFile(answer);
            }
            int[] dims2 = getWidthAndHeight(answer);
            if (formatConsole != null) {
                Thumbnails.of(answer)
                        .size(dims2[0], dims2[1])
                        .outputFormat(formatConsole)
                        .toFile(answer);
            }
            if (cropConsole != null) {
                Crop(answer, answer, cropConsole[0], cropConsole[1], cropConsole[2], cropConsole[3]);
            }
            if (blurConsole != 0) {
                GaussianBlur(answer, answer, blurConsole);
            }
            int[] dims3 = getWidthAndHeight(answer);
            Thumbnails.of(answer)
                    .size(dims3[0], dims3[1])
                    .keepAspectRatio(false)
                    .toFile(OutputPathConsole);
        } else {
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
            } catch (Exception e) {
                throw new IIOException("Can't read input file!");
            }
        }
        return 0;
    }
}
