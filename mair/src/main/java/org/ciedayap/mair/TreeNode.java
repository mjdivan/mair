/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ciedayap.mair;

import java.io.Serializable;

/**
 * It is a node related to an unidimensional binary and dense tree implementation
 * 
 * @author Mario Diván
 * @version 1.0
 */
public class TreeNode implements Serializable{
    /**
     * The nodee's ID. It must be upper or equal to 1
     */
    private final int id;
    private final boolean isLeft;    
    private String hash;
    private Integer leftchild;
    private Integer rightchild;
    private final Integer parent;
    
    public TreeNode(Integer parent,int id,boolean ileft) throws TreeNodeException
    {
        if(id<1) throw new TreeNodeException("Invalid TreeNode ID");
        
        this.id=id;
        this.isLeft=ileft;
        this.parent=parent;
        hash=null;
        leftchild=null;
        rightchild=null;                
        
    }
    
    /**
     * A factory method
     * @param parent The parent's node
     * @param id The node's id
     * @param ileft TRUE when this node is the left, FALSE otherwise
     * @param lchild The id related to the left child node
     * @param rchild The id related to the right child node
     * @return a new instance of TreeNode
     * @throws TreeNodeException it is raised when the ID is not properly defined.
     */
    public static final TreeNode create(Integer parent,int id,boolean ileft,Integer lchild,Integer rchild) throws TreeNodeException
    {
        TreeNode tn=new TreeNode(parent,id,ileft);
        tn.setLeftchild(lchild);
        tn.setRightchild(rchild);
        
        return tn;
    }
    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @return the isLeft
     */
    public boolean isIsLeft() {
        return isLeft;
    }

    /**
     * @return the hash
     */
    public String getHash() {
        return hash;
    }

    /**
     * @param hash the hash to set
     */
    public void setHash(String hash) {
        this.hash = hash;
    }

    /**
     * @return the leftchild
     */
    public Integer getLeftchild() {
        return leftchild;
    }

    /**
     * @param leftchild the leftchild to set
     */
    public void setLeftchild(Integer leftchild) {
        this.leftchild = leftchild;
    }

    /**
     * @return the rightchild
     */
    public Integer getRightchild() {
        return rightchild;
    }

    /**
     * @param rightchild the rightchild to set
     */
    public void setRightchild(Integer rightchild) {
        this.rightchild = rightchild;
    }

    /**
     * @return the parent
     */
    public Integer getParent() {
        return parent;
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb=new StringBuilder();
        sb.append("ID: ").append(this.id).append(" Parent: ").append((parent==null)?"-":parent)
                .append(" Left Child: ").append((this.leftchild==null)?"-":this.leftchild)
                .append(" Right Child: ").append((this.rightchild==null)?"-":this.rightchild)
                .append(" Hash: ").append((hash==null)?"-":hash);
        
        return sb.toString();
    }
}
