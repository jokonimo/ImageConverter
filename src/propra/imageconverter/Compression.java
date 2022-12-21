package propra.imageconverter;

public enum Compression {
    UNCOMPRESSED("uncompressed"),
    RLE("rle"),
    HUFFMANN("huffmann");

    public final String label;

    private Compression(String label) {
        this.label = label;
    }
}
