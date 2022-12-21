package propra.imageconverter;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Optional;

public class ImageConverterUtil {
    public static byte[] readFile(String inputFileName) throws IOException {
        File inputFile = new File(inputFileName);

        byte[] buf = new byte[(int) inputFile.length()];
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(inputFile));
        bis.read(buf);
        bis.close();
        return buf;
    }

    public void convert(String input, String output, byte[] buf, String outputCompression) throws IOException, IOException {
        String inputExtension = this.getFileExtension(input).get();
        String outputExtension = this.getFileExtension(output).get();

        Converter converter;
        if (inputExtension.equals("propra")) {
            converter = new PropraConverter();
            ((PropraConverter) converter).checkCheckSum(buf);
        } else if (inputExtension.equals("tga")) {
            converter = new TgaConverter();
        } else {
            System.err.println("unknown input image type");
            System.exit(123);
            return;
        }

        converter.convert(buf, output, outputExtension, outputCompression);

        try {
            converter.checkDimension(buf);
            converter.checkMissingData(buf);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.exit(123);
            return;
        }
    }

    protected Optional<String> getFileExtension(String filename) {
        return Optional.ofNullable(filename).filter(f -> f.contains("."))
                .map(f -> f.substring(filename.lastIndexOf(".") + 1));
    }
}
