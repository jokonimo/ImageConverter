package propra.imageconverter;

import java.util.Optional;

public class HuffmannTree {
    HuffmannNode root;

    public HuffmannTree(){
        root = new HuffmannNode((byte)0);
    }

    public boolean insert(HuffmannNode newNode){
        Optional<HuffmannNode> node = this.findNextFreeNode(this.root, null);
        if(node.isPresent()){
            HuffmannNode nd = node.get();
            if(nd.left == null){
                nd.left = newNode;
            }else {
                nd.right = newNode;
            }
            return true;
        }else {
            return false;
        }
    }

    public void printPreorder(HuffmannNode node)
    {
        if (node == null)
            return;

        /* first print data of node */
        System.out.print(node.key + " ");

        if(node.isBlatt){
            System.out.println("");
        }

        /* then recur on left subtree */
        printPreorder(node.left);

        /* now recur on right subtree */
        printPreorder(node.right);
    }

    public Optional<HuffmannNode> findNextFreeNode(HuffmannNode node, HuffmannNode prev){
        if(node == null){
            return Optional.of(prev);
        }

        if(node.isBlatt){
            return Optional.empty();
        }

        Optional<HuffmannNode> left = findNextFreeNode(node.left, node);

        if(left.isPresent()){
            return left;
        }else {
            return findNextFreeNode(node.right, node);
        }
    }
}
