package com.znl.pub;

/**
 * Created by Administrator on 2015/11/13.
 */
public class LinkList<T> {

    public class Node{
        private T value;
        private Node nextNode;
        public T getValue() {
            return value;
        }
        public void setValue(T value) {
            this.value = value;
        }
        public Node getNextNode() {
            return nextNode;
        }
        public void setNextNode(Node nextNode) {
            this.nextNode = nextNode;
        }
    }

    private Node _rootNode = null;

    public void addNode(T value)
    {
        if(_rootNode == null)
        {
            _rootNode = new Node();
            _rootNode.setValue(value);
        }
        else
        {
            Node newNode = new Node();
            newNode.setValue(value);
            Node curNode = this._rootNode;
            while(curNode.getNextNode() != null)
            {
                curNode = curNode.getNextNode();
            }
            curNode.setNextNode(newNode);
        }
    }

    public Node insertNode(Node node, T value){
        Node newNode = new Node();
        newNode.setValue(value);

        Node tempNode = node.getNextNode();

        node.setNextNode(newNode);
        newNode.setNextNode(tempNode);

        return newNode;
    }

    public Node getRootNode()
    {
        return _rootNode;
    }
}

