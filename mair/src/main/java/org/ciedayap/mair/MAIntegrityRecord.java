/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ciedayap.mair;

import java.security.NoSuchAlgorithmException;

/**
 * It uses a Merkel tree for implementing the integrity verification for the
 * last 2^level transactions at the Measurement Adapter (MA) level. It is important
 * to mention that MA is deployed on mobile devices.
 * 
 * @author Mario Divan
 * @version 1.0
 */
public class MAIntegrityRecord {
    public static final short ROLE_DATA_COLLECTOR=0;
    public static final short ROLE_GATEWAY=1;
    public static final short ROLE_BLOCKED=2;
    public static final short ROLE_COOPERATIVE=3;
    
    /**
     * It is a Merkel tree used to keep traceability of the transactions' integrity
     */
    private final BDTree tree;
    /**
     * It contains the current role of the measurement adapter
     */
    private short currentRole;
    
    /**
     * It creates an integrity record with the capacity to store
     * up to 2^level transactions.Because the number of retained past transactions is finite, 
 the oldest transactions go being discarded while new transactions are incorporating at the
 end of the list. This record stores the transactions at the MA level.
     * 
     * @param level The number of levels to be represented (without the root)
     * @param role The current role related to the measurement adapter
     * @throws BDTreeException It is raised when the level is negative or there no exist the unidimensional tree.
     * @throws NoSuchAlgorithmException It is raised when the MD5 algorithm does not exist.
     * @throws TreeNodeException It is raised when the node can not be created.
     */
    public MAIntegrityRecord(int level,short role) throws BDTreeException, NoSuchAlgorithmException, TreeNodeException
    {
       if(!isValidRole(role)) throw new BDTreeException("The indicated role is not defined");
       
       this.currentRole=role;
       
       tree= BDTree.create(level);
    }
    
    /**
     * It indicates whether the indicated role is valid or not
     * @param role The role to be verified
     * @return TRUE when the role is valid, FALSE otherwise.
     */
    public static boolean isValidRole(short role)
    {
       switch(role)
       {
           case MAIntegrityRecord.ROLE_BLOCKED:
           case MAIntegrityRecord.ROLE_COOPERATIVE:
           case MAIntegrityRecord.ROLE_DATA_COLLECTOR:
           case MAIntegrityRecord.ROLE_GATEWAY:
               return true;
       }      
         
       return false;
    }
    
    /**
     * It adds a transaction at the end of the list, discarding the oldest transaction.
     * 
     * @param role The current role informed for the measurement adapter
     * @param hashMD5 The MD5 related to the transaction to be added
     * @return TRUE when the new transaction has been added, FALSE otherwise
     * @throws NoSuchAlgorithmException It is raised when MD5 algorithm is not available
     */
    public synchronized Boolean addTransaction(Short role,String hashMD5) throws NoSuchAlgorithmException
    {
        if(hashMD5!=null && hashMD5.trim().length()==0) return false;
        if(!MAIntegrityRecord.isValidRole(role)) return false;
        
        this.currentRole=role;
        
        return tree.push(hashMD5);
    }
    
    /**
     * it verifies whether the root hash matches or not with the indicated hash
     * @param comeMD5 The hash to be contrasted with the root hash
     * @return TRUE when the hashes match between them, FALSE otherwise
     */
    public synchronized Boolean hasWholeIntegrity(String comeMD5)
    {
        if(comeMD5==null || comeMD5.trim().length()==0) return false;
        
        String rootHash=tree.getRootHash();
        if(rootHash==null || rootHash.trim().length()==0) return false;
        
        return rootHash.equalsIgnoreCase(comeMD5);
    }
    
    /**
     * It verifies the hash of the firsts 2^levels transactions.
     * @param hashMD5 The hash associated with the firsts 2^levels transactions
     * @param levels The number of levels related to the transactions to be contrasted. 
     * For example, if levels=1 then it will imply that the firsts 2^1=2 transactions correspond with
     * the indicated hashMD5 and it will be contrasted with the information on the tree.
     * @return TRUE when the hashes match between them, FALSE otherwise
     */
    public synchronized Boolean verifyIntegrityFirsts(String hashMD5, int levels)
    {
        if(hashMD5==null || hashMD5.trim().length()==0) return false;
        if(levels<1) return false;
        if(levels>tree.getLevels()) return false;
        
        String firsts=tree.getHashForFirstNTransactions(levels);
        if(firsts==null || firsts.trim().length()==0) return false;
        
        return firsts.equalsIgnoreCase(hashMD5);
    }

    /**
     * It verifies the hash of the lasts 2^levels transactions.
     * @param hashMD5 The hash associated with the lasts 2^levels transactions
     * @param levels The number of levels related to the transactions to be contrasted. 
     * For example, if levels=1 then it will imply that the lasts 2^1=2 transactions correspond with
     * the indicated hashMD5 and it will be contrasted with the information on the tree.
     * @return TRUE when the hashes match between them, FALSE otherwise
     */    
    public synchronized Boolean verifyIntegrityLasts(String hashMD5, int levels)
    {
        if(hashMD5==null || hashMD5.trim().length()==0) return false;
        if(levels<1) return false;
        if(levels>tree.getLevels()) return false;
        
        String lasts=tree.getHashForLastNTransactions(levels);
        if(lasts==null || lasts.trim().length()==0) return false;
        
        return lasts.equalsIgnoreCase(hashMD5);
    }
    
    /**
     * It verify the hash for a given transaction. The transactions are identified
     * considering the relative offset (i.e. 1 is the oldest and 2^levels (of the tree)
     * is the newest.
     * @param hashMD5 The hash to be verified
     * @param offset The offset of the transaction for whom the hash is associated
     * @return TRUE when the hashes match, false otherwise
     */
    public synchronized Boolean verifyTransactionIntegrity(String hashMD5, int offset)
    {
        if(offset<1) return false;
        Integer levels=tree.getLevels();
        if(levels==null || levels<1) return false;
        if(offset>BDTree.getMaxNumberOfTransactions(levels)) return false;
        
        String ohash=tree.getOffsetHash(offset);
        if(ohash==null || ohash.trim().length()==0) return false;
        return ohash.equalsIgnoreCase(hashMD5);
    }    

    /**
     * @return the currentRole
     */
    public short getCurrentRole() {
        return currentRole;
    }

    /**
     * @param currentRole the currentRole to set
     */
    public void setCurrentRole(short currentRole) {
        this.currentRole = currentRole;
    }
    
    @Override
    public String toString()
    {
        return tree.toString();
    }
}
