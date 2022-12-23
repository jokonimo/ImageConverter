package propra.imageconverter;

public class HuffmannNode {
    byte key;
    public HuffmannNode left, right, parent;

    public boolean isBlatt = false;

    public HuffmannNode(byte item)
    {
        key = item;
        left = right = parent = null;
    }
}
