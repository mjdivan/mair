/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ciedayap.mair;

import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * It implements the project integrity record, where the set of measurement adapters
 * belonging to it are contained into a concurrent hash map.
 * @author Mario Diván
 * @version 1.0
 */
public class ProjectIntegrityRecord {
    /**
     * It contains the integrity record for each measurement adapter related to the project-
     * The key is the measurement adapter ID.
     */
    private ConcurrentHashMap<String,MAIntegrityRecord> map;
    /**
     * The defaul level value for the number of transactions to keep an integrity record (2^levels).
     */
    private final int levels;
    
    /**
     * It initializes the Project Integrity record with an initial capacity of 10 measurement adapters by project.
     * @param plevels it defines the number of transactions to be contained as 2^level
     * @throws BDTreeException It is raised when the number of levels if lower than 1 
     */
    public ProjectIntegrityRecord(int plevels) throws BDTreeException
    {
        if(plevels<1) throw new BDTreeException("The indicated levels must be upper or equal to zero");
        
        levels=plevels;
        map=new ConcurrentHashMap(10);
    }

    /**
     * It initializes the project integrity rercord for the number of transactions indicated (i.e. 2^levels)
     * and containing the number of MA entries in the record.
     * @param plevels It defines the number of transactions to be managed by each MA Integrity Record  (i.e. 2^plevels)
     * @param nofMA It defines the number of measurement adapters to be contained by project
     * @throws BDTreeException It is raised when the number of levels is lower than 1
     * @throws IntegrityRecordException  It is raised when the number of measurement adapter records in the hash map is indicated under 1.
     */
    public ProjectIntegrityRecord(int plevels,int nofMA) throws BDTreeException, IntegrityRecordException
    {
        if(plevels<1) throw new BDTreeException("The indicated levels must be upper or equal to zero");
        if(nofMA<1) throw new IntegrityRecordException("The number of measurement adapters in the record should be upper or equal than 1");
            
        levels=plevels;
        map=new ConcurrentHashMap(nofMA);
    }
    
    /**
     * It adds a new hash associated with a transaction in the MAIntegrityRecord. In case of the record does not exist, it is created.
     * @param maID The measurement adapter ID
     * @param currentRole The current role related to the measurement adapterr
     * @param hashMD5 The hash associated with the transaction to be stored
     * @return TRUE when the transaction has been stored in the integrity record, FALSE otherwise
     * @throws BDTreeException It is raised when the Merkel tree cannot be created
     * @throws NoSuchAlgorithmException It is raised when the MD5 algorithm is not available on the platform
     * @throws TreeNodeException It is raised when the new node for the transaction cannot be created
     */
    public synchronized Boolean addTransaction(String maID,short currentRole, String hashMD5) throws BDTreeException, NoSuchAlgorithmException, TreeNodeException
    {
        if(maID==null || maID.trim().length()==0) return false;
        if(!MAIntegrityRecord.isValidRole(currentRole)) return false;
        if(hashMD5==null || hashMD5.trim().length()==0) return false;
        
        MAIntegrityRecord record=(map.containsKey(maID))?map.get(maID):new MAIntegrityRecord(levels,currentRole);
        
        boolean ret=record.addTransaction(currentRole, hashMD5);
        map.put(maID, record);
        
        return ret;
    }
    
    /**
     * it verifies whether the root hash matches or not with the indicated hash
     * @param maID It indicates the measurement adapter in which the verification should be made
     * @param comeMD5 The hash to be contrasted with the root hash
     * @return TRUE when the hashes match between them, FALSE otherwise
     */    
    public synchronized Boolean hasWholeIntegrity(String maID,String comeMD5)
    {
        if(maID==null || maID.trim().length()==0) return false;
        if(comeMD5==null || comeMD5.trim().length()==0) return false;
        
        MAIntegrityRecord record=map.get(maID);
        if(record==null) return false;
                
        return record.hasWholeIntegrity(comeMD5);
    }
    
    /**
     * It verifies the hash of the firsts 2^levels transactions.
     * @param maID It indicates the measurement adapter in which the verification should be made
     * @param hashMD5 The hash associated with the firsts 2^levels transactions
     * @param levels The number of levels related to the transactions to be contrasted. 
     * For example, if levels=1 then it will imply that the firsts 2^1=2 transactions correspond with
     * the indicated hashMD5 and it will be contrasted with the information on the tree.
     * @return TRUE when the hashes match between them, FALSE otherwise
     */    
    public synchronized Boolean verifyIntegrityFirsts(String maID,String hashMD5, int levels)
    {
        if(maID==null || maID.trim().length()==0) return false;
        if(levels<1) return false;
        if(hashMD5==null || hashMD5.trim().length()==0) return false;
        
        MAIntegrityRecord record=map.get(maID);
        if(record==null) return false;
                
        return record.verifyIntegrityFirsts(hashMD5, levels);
    }
    
    /**
     * It verifies the hash of the lasts 2^levels transactions.
     * @param maID It indicates the measurement adapter in which the verification should be made
     * @param hashMD5 The hash associated with the lasts 2^levels transactions
     * @param levels The number of levels related to the transactions to be contrasted. 
     * For example, if levels=1 then it will imply that the lasts 2^1=2 transactions correspond with
     * the indicated hashMD5 and it will be contrasted with the information on the tree.
     * @return TRUE when the hashes match between them, FALSE otherwise
     */        
    public synchronized Boolean verifyIntegrityLasts(String maID, String hashMD5, int levels)
    {
        if(maID==null || maID.trim().length()==0) return false;
        if(levels<1) return false;
        if(hashMD5==null || hashMD5.trim().length()==0) return false;
        
        MAIntegrityRecord record=map.get(maID);
        if(record==null) return false;
                
        return record.verifyIntegrityLasts(hashMD5, levels);
    }
    
    /**
     * It verify the hash for a given transaction.The transactions are identified
        considering the relative offset (i.e. 1 is the oldest and 2^levels (of the tree)
        is the newest.
     * @param maID It indicates the measurement adapter in which the verification should be made
     * @param hashMD5 The hash to be verified
     * @param offset The offset of the transaction for whom the hash is associated
     * @return TRUE when the hashes match, false otherwise
     */    
    public synchronized Boolean verifyTransactionIntegrity(String maID, String hashMD5, int offset)
    {
        if(maID==null || maID.trim().length()==0) return false;
        if(hashMD5==null || hashMD5.trim().length()==0) return false;
        if(offset<1) return false;
        
        MAIntegrityRecord record=map.get(maID);
        if(record==null) return false;
                
        return record.verifyTransactionIntegrity(hashMD5, offset);
    }    
    
    /**
     * It returns a string version of the merkle tree associated with the indicated MA
     * @param maID The measurement adapter to be analyzed
     * @return A string representation of the merkle tree for the indicated measurement adapter 
     */
    public String show(String maID)
    {
        if(map==null) return null;

        MAIntegrityRecord record=map.get(maID);
        
        return (record==null)?null:record.toString();        
    }
}
