package termproject;

/**
 * Title:        Term Project 2-4 Trees
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:
 * @author
 * @version 1.0
 */
public class TwoFourTree
        implements Dictionary {

    private Comparator treeComp;
    private int size = 0;
    private TFNode treeRoot = null;

    public TwoFourTree(Comparator comp) {
        treeComp = comp;
    }

    private TFNode root() {
        return treeRoot;
    }

    private void setRoot(TFNode root) {
        treeRoot = root;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return (size == 0);
    }

    /**
     * Searches dictionary to determine if key is present
     * @param key to be searched for
     * @return object corresponding to key; null if not found
     */
    public Object findElement(Object key) {
        return null;
    }
    /**
     * Searches tree to find the node where a key resides (or should)
     * @param key to be searched for
     * @return node corresponding to key
     */
    private TFNode findNode(TFNode startNode, Object key) {
        TFNode currentNode = startNode;
        int index = findFirstGreaterThanOrEqual(currentNode, key);
        //If index is greater than or equal to the num of items, then this is clearly out of bounds and this is the correct node to end on
        //Go down the tree and check for null values, if null, this is the correct node to end on
         if (currentNode.getChild(index) == null) {
            return currentNode;
          }
          else {
             if (index < currentNode.getNumItems() && treeComp.isEqual(currentNode.getItem(index).key(), key)) {
                return currentNode;
             }
             //Keep walking the tree
             return findNode(currentNode.getChild(index), key);
         }
        
    }
    
    private TFNode findInorderSuccessor(TFNode node, Object key) {
       int keyIndex = 0;
       for (int i = 0; i < node.getNumItems(); i++) {
           if (node.getItem(i) == key) {
               keyIndex = i;
               break;
           }
       }
       TFNode newTraversalNode = node.getChild(keyIndex+1);
       while (newTraversalNode.getChild(0) != null) {
           //Keep getting the leftmost child until no longer possible
           newTraversalNode = newTraversalNode.getChild(0);
       }
       return newTraversalNode;
    }

    /**
     * Inserts provided element into the Dictionary
     * @param key of object to be inserted
     * @param element to be inserted
     */
    public void insertElement(Object key, Object element) {
        if (!treeComp.isComparable(key)) {
            throw new InvalidIntegerException("This key is invalid for the given tree.");
        }
        TFNode insertionNode = new TFNode();
        Item elementItem;
        elementItem = new Item(key, element);
        if (root() == null) {
            insertionNode.addItem(0, elementItem);
            treeRoot = insertionNode;
            return;
        }
        insertionNode = findNode(root(), key);
        if (insertionNode.getChild(0) != null) {
            insertionNode = findInorderSuccessor(insertionNode, key);
        }
        int insertionIndex = findFirstGreaterThanOrEqual(insertionNode, key);
        insertionNode.insertItem(insertionIndex, elementItem);
        if (insertionNode.getNumItems() == insertionNode.getMaxItems()+1) {
            //Fix overflow or something
            fixOverflow(insertionNode);
        }
        
    }
    
    private void fixOverflow (TFNode fullNode) {
        TFNode parent;
        Item itemSplitter = fullNode.getItem(2);
        if (fullNode.getParent() == null) {
            TFNode newParent = new TFNode();
            newParent.addItem(0, itemSplitter);
            parent = newParent;
            treeRoot = parent;
        }
        else {
            parent = fullNode.getParent();
            parent.insertItem(whatChildIsThis(fullNode), itemSplitter);
        }
        TFNode newSibling = new TFNode();
        newSibling.setParent(parent);
        newSibling.addItem(0, fullNode.deleteItem(3));
        fullNode.deleteItem(2);
        //Sibling gets fullNode children 3 and 4
        TFNode child3 = fullNode.getChild(3);
        TFNode child4 = fullNode.getChild(4);
        if (child3 != null) {
            child3.setParent(newSibling);
        }
        if (child4 != null) {
            child4.setParent(newSibling);
        }
        newSibling.setChild(0, child3);
        newSibling.setChild(1, child4);
        fullNode.setChild(3, null);
        fullNode.setChild(4, null);
        int parentResetIndex = 0;
        //If this full node is not the root, get it's parent
        if (fullNode.getParent() != null) {
            parentResetIndex = whatChildIsThis(fullNode);
        }
        parent.setChild(parentResetIndex, fullNode);
        parent.setChild(parentResetIndex+1, newSibling);
        //If former root, we want to set parent at the very end to avoid issues
        fullNode.setParent(parent);
        if (parent.getNumItems() == parent.getMaxItems()+1) {
            fixOverflow(parent);
        }
        
    }

    /**
     * Searches dictionary to determine if key is present, then
     * removes and returns corresponding object
     * @param key of data to be removed
     * @return object corresponding to key
     * @exception ElementNotFoundException if the key is not in dictionary
     */
    public Object removeElement(Object key) throws ElementNotFoundException {
        TFNode removalNode = new TFNode();
        removalNode = findNode(root(), key);
        boolean found  = false;
        int removalIndex = 0;
        Object removalElement = null;
        for (int i = 0; i < removalNode.getNumItems(); i++) {
            if (treeComp.isEqual(removalNode.getItem(i).key(), key)) {
                found = true;
                removalElement = removalNode.getItem(i).element();
                removalIndex = i;
            }
        }
        if (!found) {
            throw new ElementNotFoundException("Key was not found within the tree.");
        }
        TFNode inorderSuccessor = null;
        if (removalNode.getChild(0) != null) {
            inorderSuccessor = findInorderSuccessor(removalNode, key);
        }
        
        if (inorderSuccessor != null) {
            removalNode.deleteItem(removalIndex);
            //Move the smallest item within the inorder successor to removal node.
            removalNode.addItem(removalIndex, inorderSuccessor.removeItem(0));
            if (inorderSuccessor.getNumItems() == 0) {
                fixUnderflow(inorderSuccessor);
            }
        }
        else {
            removalNode.removeItem(removalIndex);
            if (removalNode.getNumItems() == 0) {
                fixUnderflow(inorderSuccessor);
            }
        }
        return removalElement;
        
    }
    
    private void fixUnderflow(TFNode emptyNode) {
        TFNode parent = emptyNode.getParent();
        TFNode leftSib = null;
        TFNode rightSib = null;
        if (parent == null) {
            return;
        }
        int childIndex = whatChildIsThis(emptyNode);
        if (childIndex - 1 > -1) {
            leftSib = parent.getChild(childIndex - 1);
        }
        rightSib = parent.getChild(childIndex + 1);
        if (leftSib != null && leftSib.getNumItems() > 1) {
            leftTransfer(emptyNode, leftSib, parent);
        }
        else if (rightSib != null && rightSib.getNumItems() > 1) {
            rightTransfer(emptyNode, rightSib, parent);
        }
        else if (leftSib != null) {
            
        }
        else {
            
        }
    }
    
    private void leftTransfer(TFNode original, TFNode leftSib, TFNode parent) {
        Item pivotItem = leftSib.deleteItem(leftSib.getNumItems()-1);
        int pivotIndex = whatChildIsThis(leftSib);
        //Need to insert item to shift the child pointers to provide the hole for the new child
        original.insertItem(0, parent.deleteItem(pivotIndex));
        parent.addItem(pivotIndex, pivotItem);
        //Switch the child of the left sib
        TFNode leftSibChild = leftSib.getChild(leftSib.getNumItems()+1);
        leftSibChild.setParent(original);
        original.setChild(0, leftSibChild);
    }
    
    private void rightTransfer(TFNode original, TFNode rightSib, TFNode parent) {
        TFNode rightSibChild = rightSib.getChild(0);
        Item pivotItem = rightSib.removeItem(0);
        int pivotIndex = whatChildIsThis(rightSib);
        //Need to insert item to shift the child pointers to provide the hole for the new child
        original.addItem(0, parent.deleteItem(pivotIndex));
        parent.addItem(pivotIndex, pivotItem);
        //Switch the child of the right sib
        rightSibChild.setParent(original);
        original.setChild(0, rightSibChild);
    }

    public static void main(String[] args) {
        Comparator myComp = new IntegerComparator();
        TwoFourTree myTree = new TwoFourTree(myComp);

        Integer myInt1 = new Integer(47);
        myTree.insertElement(myInt1, myInt1);
        Integer myInt2 = new Integer(83);
        myTree.insertElement(myInt2, myInt2);
        Integer myInt3 = new Integer(22);
        myTree.insertElement(myInt3, myInt3);
        
        Integer myInt4 = new Integer(16);
        myTree.insertElement(myInt4, myInt4);
        
        Integer myInt5 = new Integer(49);
        myTree.insertElement(myInt5, myInt5);
        

        Integer myInt6 = new Integer(100);
        myTree.insertElement(myInt6, myInt6);
        
        
        Integer myInt7 = new Integer(38);
        myTree.insertElement(myInt7, myInt7);
        
        
        

        Integer myInt8 = new Integer(3);
        myTree.insertElement(myInt8, myInt8);
        
        myTree.printAllElements();
        myTree.checkTree();

        Integer myInt9 = new Integer(22);
        myTree.insertElement(myInt9, myInt9);
        
        myTree.removeElement(22);
        myTree.printAllElements();
        myTree.checkTree();
        
/*
        Integer myInt10 = new Integer(66);
        myTree.insertElement(myInt10, myInt10);

        Integer myInt11 = new Integer(19);
        myTree.insertElement(myInt11, myInt11);

        Integer myInt12 = new Integer(23);
        myTree.insertElement(myInt12, myInt12);

        Integer myInt13 = new Integer(24);
        myTree.insertElement(myInt13, myInt13);

        Integer myInt14 = new Integer(88);
        myTree.insertElement(myInt14, myInt14);

        
        Integer myInt15 = new Integer(1);
        myTree.insertElement(myInt15, myInt15);

        myTree.printAllElements();
        myTree.checkTree();
        
        Integer myInt16 = new Integer(97);
        myTree.insertElement(myInt16, myInt16);

        Integer myInt17 = new Integer(94);
        myTree.insertElement(myInt17, myInt17);

        Integer myInt18 = new Integer(35);
        myTree.insertElement(myInt18, myInt18);

        Integer myInt19 = new Integer(51);
        myTree.insertElement(myInt19, myInt19);
        System.out.println("Final Tree: ");
        myTree.printAllElements();
        myTree.checkTree();
        System.out.println("done");

        myTree = new TwoFourTree(myComp);
        final int TEST_SIZE = 10000;


        for (int i = 0; i < TEST_SIZE; i++) {
            myTree.insertElement(new Integer(i), new Integer(i));
            //          myTree.printAllElements();
            //         myTree.checkTree();
        }
        System.out.println("Final Tree: ");
        myTree.printAllElements();
        myTree.checkTree();
        System.out.println("done");
        
        System.out.println("removing");
        for (int i = 0; i < TEST_SIZE; i++) {
            int out = (Integer) myTree.removeElement(new Integer(i));
            if (out != i) {
                throw new TwoFourTreeException("main: wrong element removed");
            }
            if (i > TEST_SIZE - 15) {
                myTree.printAllElements();
            }
        }
        System.out.println("done");
*/
    }

    public void printAllElements() {
        int indent = 0;
        if (root() == null) {
            System.out.println("The tree is empty");
        }
        else {
            printTree(root(), indent);
        }
    }

    public void printTree(TFNode start, int indent) {
        if (start == null) {
            return;
        }
        for (int i = 0; i < indent; i++) {
            System.out.print(" ");
        }
        printTFNode(start);
        indent += 4;
        int numChildren = start.getNumItems() + 1;
        for (int i = 0; i < numChildren; i++) {
            printTree(start.getChild(i), indent);
        }
    }

    public void printTFNode(TFNode node) {
        int numItems = node.getNumItems();
        for (int i = 0; i < numItems; i++) {
            System.out.print(((Item) node.getItem(i)).element() + " ");
        }
        System.out.println();
    }

    // checks if tree is properly hooked up, i.e., children point to parents
    public void checkTree() {
        checkTreeFromNode(treeRoot);
    }

    private void checkTreeFromNode(TFNode start) {
        if (start == null) {
            return;
        }

        if (start.getParent() != null) {
            TFNode parent = start.getParent();
            int childIndex = 0;
            for (childIndex = 0; childIndex <= parent.getNumItems(); childIndex++) {
                if (parent.getChild(childIndex) == start) {
                    break;
                }
            }
            // if child wasn't found, print problem
            if (childIndex > parent.getNumItems()) {
                System.out.println("Child to parent confusion");
                printTFNode(start);
            }
        }

        if (start.getChild(0) != null) {
            for (int childIndex = 0; childIndex <= start.getNumItems(); childIndex++) {
                if (start.getChild(childIndex) == null) {
                    System.out.println("Mixed null and non-null children");
                    printTFNode(start);
                }
                else {
                    if (start.getChild(childIndex).getParent() != start) {
                        System.out.println("Parent to child confusion");
                        printTFNode(start);
                    }
                    for (int i = childIndex - 1; i >= 0; i--) {
                        if (start.getChild(i) == start.getChild(childIndex)) {
                            System.out.println("Duplicate children of node");
                            printTFNode(start);
                        }
                    }
                }

            }
        }

        int numChildren = start.getNumItems() + 1;
        for (int childIndex = 0; childIndex < numChildren; childIndex++) {
            checkTreeFromNode(start.getChild(childIndex));
        }

    }
    
    private int findFirstGreaterThanOrEqual(TFNode node, Object key) {
        int i;
        for (i=0; i< node.getNumItems(); i++) {
            if (treeComp.isGreaterThanOrEqualTo(node.getItem(i).key(), key)) {
                break;
            } 
        }
        return i;
    } 
    
    private int whatChildIsThis(TFNode node) {
        TFNode parent = node.getParent();
        int i;
        for (i=0; i<parent.getNumItems()+1; i++) {
            if (parent.getChild(i) == node) {
                return i;
            }
        }
        return -1;

    }
}