/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ciedayap.mair;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import org.apache.commons.lang3.SerializationUtils;

/**
 * It is the implementation of a binary and dense tree following the concepts of a Merkle Tree.
 * The implementation is stored in a unidimensional array mapping the positions of each node.
 * All the nodes are consecutively enumerated starting in 1.
 * 
 * @author Mario José Diván
 * @version 1.0
 */
public class BDTree {
    public static final int CHILD_LEFT=0;
    public static final int CHILD_RIGHT=1;
    
    private final MessageDigest md5;
    private final TreeNode tree[];
    private final Integer levels; 
    
    /**
     * The dense and binary Merkle tree is created.
     * @param power The number of levels to be represented (without the root)
     * @throws org.ciedayap.mair.BDTreeException  It is raised when the indicated power is lower than 1
     * @throws java.security.NoSuchAlgorithmException It is raised when the MD5 algorithm is not available
     * @throws org.ciedayap.mair.TreeNodeException It is raised when there is not father for the root or father for the rest of the nodes
     */
    public BDTree(Integer power) throws BDTreeException, NoSuchAlgorithmException, TreeNodeException
    {
        if(power==null || power<1) throw new BDTreeException("The power must be equal or upper than 1");
        md5=MessageDigest.getInstance("MD5");
        
        if(power>25) throw new BDTreeException("It is not recommendable such a level of record in a mobile device");
        tree=createBDTree(power);
        levels=power;
    }
    
    /**
     * It creates a new BDTree structure storing the indicated TreeNode array as the new tree for the {power} levels indicated.
     * @param power The number of tree's levels. The capacity will be given by 2^power elements
     * @param tr The Unidimensional representation of the BDTree 
     * @throws BDTreeException It is raised when the level is negatiive or there no exist the unidimensional tree.
     * @throws NoSuchAlgorithmException It is raised when the MD5 algorithm does not exist.
     */
    public BDTree(Integer power,TreeNode[] tr) throws BDTreeException, NoSuchAlgorithmException
    {
        if(power==null || power<1) throw new BDTreeException("The power must be equal or upper than 1");
        md5=MessageDigest.getInstance("MD5");
        
        Integer nnodes=BDTree.getTotalRequiredNodes(power);
        if(tr==null) throw new BDTreeException("The tree is null");
        if(nnodes!=tr.length) throw new BDTreeException("There is not correspondence between the informed tree and the indicated levels");
        
        tree=tr;
        levels=power;
    }
    
    /**
     * In initializes the complete BDTree structure for the given level
     * @param power The number of levels of the tree 
     * @return An unidimensional Dense and Binary Tree
     * @throws BDTreeException It is raised when some mistake happens throughout the tree creation (e.g. it is not possible to compute the childs or father)
     * @throws TreeNodeException It is raised when the node can not be created.
     */
    private TreeNode[] createBDTree(Integer power) throws BDTreeException, TreeNodeException
    {
        Integer nnodes=BDTree.getTotalRequiredNodes(power);
        TreeNode[] temp=new TreeNode[nnodes];
        
        int enumerator=1;
        for(int i=0;i<temp.length;i++)
        {
           int childs[]=BDTree.childsOf(i+1,power);           

            if(i==0)
           {
               if(childs==null) throw new BDTreeException("There no exists childs for the root");
               
               temp[i]=TreeNode.create((Integer)null, i+1, true, childs[BDTree.CHILD_LEFT],childs[BDTree.CHILD_RIGHT]);
           }
           else
           {
               int father=BDTree.fatherOf(i+1);
               
               if(father<1) throw new BDTreeException("There no exists father for the nodeID "+i);
               
               temp[i]=TreeNode.create(father, i+1, BDTree.isLeftNode(i+1), (childs==null)?null:childs[BDTree.CHILD_LEFT],
                       (childs==null)?null:childs[BDTree.CHILD_RIGHT]);
           }           
        }
        
        return temp;
    }
    
    /**
     * It creates a new instance of a clean BDTree.
     * @param levels The number of levels to be represented (without the root)
     * @return a new instance of a BDTree
     * @throws BDTreeException It is raised when the level is negative or there no exist the unidimensional tree.
     * @throws NoSuchAlgorithmException It is raised when the MD5 algorithm does not exist.
     * @throws TreeNodeException It is raised when the node can not be created.
     */
    public static synchronized BDTree create(int levels) throws BDTreeException, NoSuchAlgorithmException, TreeNodeException
    {
        return new BDTree(levels);
    }
    
    /**
     * It returns the total number of required nodes in a unidimensional array for representing the indicated levels
     * @param levels The number of levels to be managed (without the root)
     * @return The total number of nodes required in a unidimensional array, considering leaf and intermediary nodes.
     */
    public static final int getTotalRequiredNodes(int levels)
    {
        if(levels<1) return -1;
        
        return  (int)(Math.pow(2, levels+1)-1);
    }
    
    /**
     * It returns the level of elements in the tree for managing the indicated number of elements
     * @param elements The number of elements to bee managed in the tree
     * @return the number of required levels by the tree. It returns -1 when the number of elements is under 1.
     */
    public static final int getLevelsForNElements(int elements)
    {
        if(elements<1) return -1;
        if(elements==1) return 1;
        
        double res=Math.log(elements)/Math.log(2);
                
        res=Math.ceil(res);
        
        return (int)res;
    }
    
    /**
     * It indicates the max number of transactions to be represented using
     * the indicated number of levels
     * @param levels The number of levels to be used
     * @return The max number of transactions to be represented
     */
    public static final int getMaxNumberOfTransactions(int levels)
    {
        if(levels<1) return -1;
        
        return (int)Math.pow(2, levels);
    }
    /**
     * It returns the node ID who share the same father
     * @param nodeid the nodeID to be verified (upper or equal to 1)
     * @return the brother's node ID when it is present, -1 otherwise
     */
    public static final int whoIsMyBrother(int nodeid)
    {
        if(nodeid<1) return -1;
        int father=BDTree.fatherOf(nodeid);
        if(father<0) return -1;
        
        int[] childs=BDTree.childsOf(father);
        if(childs==null) return -1;
        
        return (childs[BDTree.CHILD_LEFT]==nodeid)?childs[BDTree.CHILD_RIGHT]:childs[BDTree.CHILD_LEFT];
    }

    /**
     * It returns the computed hash associated with the root node
     * @return The hash associated with the root node (it could be null when the tree has not any value)
     */
    public String getRootHash()
    {
        if(levels<1) return null;
        if(tree==null) return null;
        if(tree[0]==null) return null;
        
        return tree[0].getHash();
    }
    
    /**
     * It returns the hash related to the indicated node ID
     * @param nodeid The node ID to know its hash
     * @return The hash when the node ID is found, null otherwise
     */
    public String getNodeHash(int nodeid)
    {
        if(nodeid<1) return null;
        if((nodeid-1)>this.tree.length) return null;
        
        return (tree[nodeid-1]!=null)?tree[nodeid-1].getHash():null;
    }
    
    /**
     * Given a level, the number of transactions to be represented is associated
     * with [1; 2^level]. This function returns the hash related to a given offset
     * contained in such an interval.
     * @param offset The offset to be verified
     * @return The hash associated with the offset
     */
    public String getOffsetHash(int offset)
    {
        if(offset<1) return null;
        if(levels==null || levels<1) return null;
        if(offset>BDTree.getMaxNumberOfTransactions(levels)) return null;
        
        int roffset=offset-1;
        
        int oldestLeaf=BDTree.getInitialNodeByLevel(levels);
        
        return tree[(oldestLeaf-1)+roffset].getHash();
    }
    
    /**
     * It returns the hash that represents the integrity value for the last 
     * 2^qlevel transactions. this is useful when the leaves are ordered according
     * to their arrival because the last records could be verified partially.
     * 
     * @param qlevels It is the power of two. It must be upper than 1 and lower or equal to the maxlevel of the tree.
     * For example, given a tree with 4 levels, 1 will provide the hash for the last 2 transactions  (2^1=2),
     * 2 will provide the hash for the last 4 transactions (2^2=4), 3 will provide the hash for the last 8 transactions
     * (2^3=8), and so on successively. 
     * @return The hash related to the last 2^{@qlevels} transactions. 
     * null otherwise (there no exists enough information)
     */
    public String getHashForLastNTransactions(int qlevels)
    {
       if(qlevels<1) return null;
       if(qlevels>levels) return null;
       
       int differ=levels-qlevels;
       
       if(differ==0) return (tree[0]!=null)?tree[0].getHash():null;
       
       int currentNode=1;//Start from the root
       while(differ>0)
       {
           int childs[]=BDTree.childsOf(currentNode);
           currentNode=childs[BDTree.CHILD_RIGHT];
           differ--;
       }
       
       return (tree[currentNode-1]!=null)?tree[currentNode-1].getHash():null;
    }
    
    /**
     * It returns the hash that represents the integrity value for the first 
     * 2^qlevel transactions. this is useful when the leaves are ordered according
     * to their arrival because the first records could be verified partially.
     * 
     * @param qlevels It is the power of two. It must be upper than 1 and lower or equal to the maxlevel of the tree.
     * For example, given a tree with 4 levels, 1 will provide the hash for the first 2 transactions  (2^1=2),
     * 2 will provide the hash for the first 4 transactions (2^2=4), 3 will provide the hash for the first 8 transactions
     * (2^3=8), and so on successively. 
     * @return The hash related to the first 2^{@qlevels} transactions. 
     * null otherwise (there no exists enough information)
     */
    public String getHashForFirstNTransactions(int qlevels)
    {
       if(qlevels<1) return null;
       if(qlevels>levels) return null;
       
       int differ=levels-qlevels;
       
       if(differ==0) return (tree[0]!=null)?tree[0].getHash():null;
       
       int currentNode=1;//Start from the root
       while(differ>0)
       {
           int childs[]=BDTree.childsOf(currentNode);
           currentNode=childs[BDTree.CHILD_LEFT];
           differ--;
       }
       
       return (tree[currentNode-1]!=null)?tree[currentNode-1].getHash():null;
    }
    
    @Override
    public BDTree clone() throws CloneNotSupportedException
    {
        if(levels==null || levels<1) return null;
        
        TreeNode[] copyOf = SerializationUtils.clone(tree);
        
        BDTree ret;
        try {
            ret = new BDTree(levels,copyOf);
        } catch (BDTreeException | NoSuchAlgorithmException ex) {
            return null;
        }
        
        return ret;
    }
        
    @Override
    public boolean equals(Object o)
    {
        if(o==this) return true;
        if(!(o instanceof BDTree)) return false;
        
        BDTree come=(BDTree)o;
        
        String hcome=come.getRootHash();
        String hthis=this.getRootHash();
        
        if(hcome==null || hthis==null) return false;
        
        return hcome.equalsIgnoreCase(hthis);        
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 11 * hash + Arrays.deepHashCode(this.tree);
        return hash;
    }
    
    /**
     * It indicates whether the nodeid is the left child or not
     * @param nodeid The node to be verified
     * @return TRUE/FALSE whether is the left/right node, null when atypical parameters are reached
     */
    public static final Boolean isLeftNode(int nodeid)
    {
        if(nodeid<1) return null;
        if(nodeid==1) return true; //It is the root
        int father=BDTree.fatherOf(nodeid);
        if(father<0) return null;
        
        int[] childs=BDTree.childsOf(father);
        if(childs==null) return null;
        
        return (childs[BDTree.CHILD_LEFT]==nodeid);
    }
    
    /**
     * It returns the father node for the indicated node ID
     * @param nodeid the node id who want to know their father
     * @return The father node ID, -1 when there is no father
     */    
    public static final int fatherOf(int nodeid)
    {
        if(nodeid<=1) return -1;
        int currentLevel=((int)(Math.log(nodeid)/Math.log(2)));
        int previousLevel=currentLevel-1;
        
        if(previousLevel<0) return -1;//Be careful...0 is the root
        int offset=nodeid-(int)(Math.pow(2, currentLevel));
        
        return ((int)Math.pow(2, previousLevel))+((int)offset/2);        
    }
    
    /**
     * It returns the childs of a given node as a unidimensional array with two positions.
     * Position 0 is associated with the left child, while the position 1 is related to the right child.
     * @param nodeid The node who want to know their childs
     * @return An unidimensional array with two positions (0: Left Child, 1: Right Child), null otherwise.
     */
    public static final int[] childsOf(int nodeid)
    {
        if(nodeid<1) return null;
        int currentLevel=((int)(Math.log(nodeid)/Math.log(2)));
        int offset=nodeid-(int)(Math.pow(2, currentLevel));
        
        int[] childs=new int[2];
        childs[BDTree.CHILD_LEFT]=((int)Math.pow(2, currentLevel+1))+(2*offset);
        childs[BDTree.CHILD_RIGHT]=childs[BDTree.CHILD_LEFT]+1;
        
        return childs;
    }
    
    /**
     * It returns the childs of a given node as a unidimensional array with two positions.Position 0 is associated with the left child, while the position 1 is related to the right child.
     * @param nodeid The node who want to know their childs
     * @param maxlevel The max level of depth related to the tree without take into consideration the root.
     * @return An unidimensional array with two positions (0: Left Child, 1: Right Child), null otherwise.
     */
    public static final int[] childsOf(int nodeid, int maxlevel)
    {
        if(nodeid<1) return null;
        if(maxlevel<1) return null;
        
        int currentLevel=((int)(Math.log(nodeid)/Math.log(2)));
        int offset=nodeid-(int)(Math.pow(2, currentLevel));

        if(nodeid>getTotalRequiredNodes(maxlevel)) return null;
        
        int[] childs=new int[2];
        childs[BDTree.CHILD_LEFT]=((int)Math.pow(2, currentLevel+1))+(2*offset);
        childs[BDTree.CHILD_RIGHT]=childs[BDTree.CHILD_LEFT]+1;
        
        if(childs[BDTree.CHILD_RIGHT]>getTotalRequiredNodes(maxlevel)) return null;
        
        return childs;
    }    

    /**
     * It indicates the nodeID in where the leafs start
     * @param level the level number  without considering the root.
     * @return The first nodedID that is a leaf
     */
    public static final int getInitialNodeByLevel(int level)
    {
        if(level<1) return -1;
        
        return (int)Math.pow(2, level);
    }

    /**
     * It indicates the nodeID in where the leafs end
     * @param level the level number without considering the root.
     * @return The last nodedID that is a leaf
     */    
    public static final int getLastNodeByLevel(int level)
    {
        if(level<1) return -1;
        
        return getTotalRequiredNodes(level);       
    }
    
    /**
     * It indicates whether the nodeid indicates is a lesf or not.
     * @param nodeid A value upper or equal to 1
     * @param maxlevel The max level of depth related to the tree without take into consideration the root.
     * @return TRUE/FALSE depending on whether is leaf or not, null otherwise when the parameter are atypical to the tree
     */
    public static final Boolean isLeaf(int nodeid, int maxlevel)
    {
        if(nodeid<1) return null;
        if(maxlevel<1) return null;
        
        if(nodeid<getInitialNodeByLevel(maxlevel)) return false;

        if(nodeid<=getLastNodeByLevel(maxlevel)) return true;
        
        return null;//out of range
    }
    
    @Override
    public String toString()
    {
        if(this.tree==null || this.tree.length==0) return "Empty Tree";
        
        StringBuilder sb=new StringBuilder();
        for(TreeNode tn:tree)
        {
            sb.append(tn.toString()).append("\n");
        }
        
        return sb.toString();
    }
    
    /**
     * It assigns a new set of hash for each leaf of the three, recomputing all the hashes
     * @param multiplehashs The set of hashes to be updated in the leafs
     * @return TRUE/FALSE depending on the possibility of update all the nodes
     * @throws BDTreeException It is raides when some anomaly in the parameters is detected
     * @throws NoSuchAlgorithmException It is raised when MD5 algorithm is not defined
     */
    public Boolean setNewHashinLeafs(String multiplehashs[]) throws BDTreeException, NoSuchAlgorithmException
    {
        if(multiplehashs==null) throw new BDTreeException("Hashes not defined");
        int init=BDTree.getInitialNodeByLevel(this.getLevels());
        int end=BDTree.getLastNodeByLevel(this.getLevels());
        int range=end-init+1;
        
        if(multiplehashs.length!=range) throw new BDTreeException("The range "+range+" is different from the list of hashes "+multiplehashs.length);
        
        int idx=init-1;
        for(String hash:multiplehashs)
        {
            tree[idx].setHash(hash);
            idx++;
        }
        
        return recomputeHashes();
    }
    
    /**
     * It recompute thee hashes for all the intermediary nodes
     * @return TRUE when all the hashes have beed recomputed, FALSE otherwise.
     * @throws NoSuchAlgorithmException When MD% algorithm is not defined
     */
    private Boolean recomputeHashes() throws NoSuchAlgorithmException
    {
        int init,end;
        int currentLevel=getLevels()-1;
        StringBuilder sb=new StringBuilder();
        while(currentLevel>=1)
        {
            init=BDTree.getInitialNodeByLevel(currentLevel);
            end=BDTree.getLastNodeByLevel(currentLevel);
            //System.out.println("L:"+currentLevel+" i:"+init+" e:"+end);
            for(int i=init;i<=end;i++)
            {
                int childs[]=BDTree.childsOf(i);
                if(childs!=null)
                {                    
                    if(tree[childs[BDTree.CHILD_LEFT]-1].getHash()!=null)
                    {
                        sb.append(tree[childs[BDTree.CHILD_LEFT]-1].getHash());
                        if(tree[childs[BDTree.CHILD_RIGHT]-1].getHash()!=null)
                        {
                            sb.append(".")
                              .append(tree[childs[BDTree.CHILD_RIGHT]-1].getHash());
                            
                            tree[i-1].setHash(computeHash(sb.toString()));                            
                        }
                        else
                        {
                            tree[i-1].setHash(sb.toString());//It is not necessary to recompute
                        }                        
                    }
                    else
                    {
                        if(tree[childs[BDTree.CHILD_RIGHT]-1].getHash()!=null)
                        {
                            tree[i-1].setHash(tree[childs[BDTree.CHILD_RIGHT]-1].getHash());//It is not necessary to recompute
                        }
                        else
                        {
                            tree[i-1].setHash(null);
                        }
                    }
                    
                    sb.delete(0, sb.length());                    
                }
            }
            
            currentLevel--;
        }
        
        if(tree[1].getHash()!=null)
        {
            sb.append(tree[1].getHash());
            if(tree[2].getHash()!=null)
            {
                sb.append(".").append(tree[2].getHash());
                tree[0].setHash(computeHash(sb.toString()));
            }
            else
            {
                tree[0].setHash(tree[1].getHash());
            }
        }
        else
        {
            if(tree[2].getHash()!=null)
            {
                tree[0].setHash(tree[2].getHash());
            }
            else
            {
                tree[0].setHash(null);
            }            
        }
        sb.delete(0, sb.length());
        
        return true;
    }
    
    /**
     * It updates the hash in an individual leaf and recomputes the hash in the path up to the root
     * 
     * @param offset It is the position of the element in the list of lead starting from 1 to...
     * @param hash The new hash expressed as MD5
     * @return TRUE/FALSE depending on the change has been made. Null wheen atypical parameters are received.
     * @throws java.security.NoSuchAlgorithmException It is raised when the MD5 algorithm is not present
     */
    public Boolean setNewHashinLeaf(int offset,String hash) throws NoSuchAlgorithmException
    {
        if(offset<0) return null;
        
        int init=BDTree.getInitialNodeByLevel(this.getLevels());
        int end=BDTree.getLastNodeByLevel(this.getLevels());
        int range=end-init+1;
        
        if(offset>range) return null;
        
        return updateHash(init+(offset-1),hash);
        
    }
    /**
     * It updates the path starting from the node's hash modified
     * @param nodeid The node in where the hash has changed
     * @param hash The new hash related to the nodeID
     * @return TRUE/FALSE depending on the change has been made. Null wheen atypical parameters are received.
     */
    private Boolean updateHash(int nodeid,String hash) throws NoSuchAlgorithmException
    {
        if(tree==null || tree.length==0) return null;        
        int idx=nodeid-1;
        if(idx<0 || idx>tree.length) return null;
        
        tree[idx].setHash(hash);
        
        int parent=BDTree.fatherOf(nodeid);
        if(parent<0) return true;
        
        int[] childs=BDTree.childsOf(parent);
        if(childs==null) return false;
        
        StringBuilder newHash=new StringBuilder();
        if(tree[childs[BDTree.CHILD_LEFT]-1].getHash()!=null)
        {
            newHash.append(tree[childs[BDTree.CHILD_LEFT]-1].getHash());
            
            if(tree[childs[BDTree.CHILD_RIGHT]-1].getHash()!=null)
            {
                newHash.append(".")
                        .append(tree[childs[BDTree.CHILD_RIGHT]-1].getHash());
                
                return updateHash(parent,computeHash(newHash.toString()));
            }
            else
            {
                return updateHash(parent,tree[childs[BDTree.CHILD_LEFT]-1].getHash());
            }
        }
        else
        {
            if(tree[childs[BDTree.CHILD_RIGHT]-1].getHash()!=null)
            {
                return updateHash(parent,tree[childs[BDTree.CHILD_RIGHT]-1].getHash());
            }
            else
            {
                return false;//IZQ and DER are null
            }
        }        
    }
    
        /**
     * It computes a MD5 hash from the brief message
     * @param hash the String to be computed
     * @return The hash expressed as a hexadecimal String
     * @throws NoSuchAlgorithmException When MD5 is not defined
     */
    private String computeHash(String hash) throws NoSuchAlgorithmException
    {       
        if(hash==null || hash.trim().length()==0) return null;        
        if(md5==null) return null;
                
        md5.update(hash.getBytes());
        
        return toHexString(md5.digest());
    }
    
    /**
     * It converts the hexadecimal to a String representaiton
     * @param bytes The hexadecimal to be converted
     * @return A string representing the hash as a hexadecimal 
     */
    public static String toHexString(byte[] bytes) 
    {
      if(bytes==null || bytes.length==0) return null;

      StringBuilder hexString = new StringBuilder();

      for (byte myByte: bytes) 
      {
          String hex = Integer.toHexString(0xFF & myByte);
          if (hex.length() == 1) hexString.append('0');

          hexString.append(hex);
      }

      return hexString.toString();
    }    
    
    /**
     * It scrolls left the hash of the leafs, discarding the first one (the old transaction) 
     * and appending at the end the newHash as the most recet transacction  (last node)
     * @param newHash The new hash to be incorporated
     * @return TRUE/FALSE depending on the change has been made. Null wheen atypical parameters are received.
     * @throws NoSuchAlgorithmException It is raised when the MD5 algorithm is not present
     */
    public synchronized Boolean push(String newHash) throws NoSuchAlgorithmException
    {
        if(levels<1) return false;
        if(tree==null) return false;
        
        int init=BDTree.getInitialNodeByLevel(this.getLevels());
        int end=BDTree.getLastNodeByLevel(this.getLevels());
        
        for(int i=init+1;i<=end;i++)
        {
            tree[i-2].setHash(tree[i-1].getHash());
        }
        tree[end-1].setHash(newHash);
        
        return recomputeHashes();

    }
    
    /**
     * @return the levels
     */
    public Integer getLevels() {
        return levels;
    }
        
    public static void main(String args[]) throws BDTreeException, NoSuchAlgorithmException, TreeNodeException, CloneNotSupportedException
    {/*
        System.out.println(getLevelsForNElements(16));
        System.out.println((int)Math.pow(2,getLevelsForNElements(16)));        
        System.out.println(BDTree.getTotalRequiredNodes(getLevelsForNElements(16)));        
        
        int[] viene=BDTree.childsOf(27,4);
        if(viene==null) System.out.println("There no exist childs");
        else System.out.println("LEFT: "+viene[0]+" R: "+viene[1]);
        System.out.println(getTotalRequiredNodes(4));
         
        viene=BDTree.childsOf(15,4);
        if(viene==null) System.out.println("There no exist childs");
        else System.out.println("LEFT: "+viene[0]+" R: "+viene[1]);

        System.out.println(isLeaf(28,4)+" 1st leaf:  "+BDTree.getInitialNodeByLevel(4)+" LastLeaf: "+BDTree.getLastNodeByLevel(4));
        System.out.println(isLeaf(55,4)+" 1st leaf:  "+BDTree.getInitialNodeByLevel(4)+" LastLeaf: "+BDTree.getLastNodeByLevel(4));
        System.out.println(isLeaf(13,4)+" 1st leaf:  "+BDTree.getInitialNodeByLevel(4)+" LastLeaf: "+BDTree.getLastNodeByLevel(4));
        
        for(int i=1;i<=4;i++)
        {
          System.out.println("Level: "+i+" 1st node:  "+BDTree.getInitialNodeByLevel(i)+" Lastnode: "+BDTree.getLastNodeByLevel(i));
        }
        
        int nodeid=1;
        int father=BDTree.fatherOf(nodeid);
        System.out.println("father: "+father);

        
        int[] childs=BDTree.childsOf(nodeid);
        if(childs==null) System.out.println("No childs");
        else {
            System.out.println("LEFT: "+childs[0]+" R: "+childs[1]);
            System.out.println( (childs[BDTree.CHILD_LEFT]==nodeid)?childs[BDTree.CHILD_RIGHT]:childs[BDTree.CHILD_LEFT]);
        }        
        
        System.out.println("NodeID: "+2+" Brother: "+BDTree.whoIsMyBrother(2));
        System.out.println("NodeID: "+2+" Left?: "+BDTree.isLeftNode(2));
        System.out.println("NodeID: "+5+" Left?: "+BDTree.isLeftNode(5));*/
        
        BDTree mytree=new BDTree(4);
        
        //mytree.setNewHashinLeaf(16,"hola");
        //mytree.setNewHashinLeaf(13,"jose");
        String hashes[]={"1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16"};
        mytree.setNewHashinLeafs(hashes);
        System.out.println(mytree.toString());
        
        BDTree mytree2=mytree.clone();
        if(mytree2.equals(mytree)) System.out.println("Clonado OK");
        
        long start=System.nanoTime();
        mytree.push("Ultimoooo");
        long end=System.nanoTime();
        System.out.println("Total Time: "+(end-start));
        
        //System.out.println(mytree.toString());        

        
        //InstrumentationAgent ia=new InstrumentationAgent();
        //System.out.println("Bytes mytree2: "+ia.sizeDeepOf(mytree2));
        
        /*for(int i=1;i<=4;i++)
        {
            System.out.println("Last "+Math.pow(2, i)+" transactions, Hash: "+mytree2.getHashForLastNTransactions(i));
            System.out.println("First "+Math.pow(2, i)+" transactions, Hash: "+mytree2.getHashForFirstNTransactions(i));
        }*/
        mytree2.push("nuevo");
        System.out.println(mytree2.toString());           
        //for(int i=1;i<=16;i++)
          //  System.out.println("i: "+i+" H: "+mytree2.getOffsetHash(i));
        
        System.out.println(mytree2.getOffsetHash(0));
        System.out.println(mytree2.getOffsetHash(17));
        System.out.println(mytree2.getOffsetHash(7));
    }
    
}
