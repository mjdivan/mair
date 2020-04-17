/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ciedayap.mair;

import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author mjdivan
 */
public class GlobalIntegrityRecord {
    /**
     * It contains the integrity record for each project related to the project-
     * The key is the measurement adapter ID.
     */    
    private ConcurrentHashMap<String,ProjectIntegrityRecord> map;
    /**
     * The defaul level value for the number of transactions to keep an integrity record (2^levels).
     */    
    private final int levels;
    /**
     * The defaul level value for the number of measurement adapters per project.
     */        
    private final int numberOfMA;
    
    /**
     * It initializes the Global Integrity record with an initial capacity of 10 projects.
     * @param plevels it defines the number of transactions to be contained as 2^level
     * @param nOfMA The default value for the number of measurement adapters per project
     * @throws BDTreeException It is raised when the number of levels if lower than 1 
     * @throws org.ciedayap.mair.IntegrityRecordException It is raised when the nOfMA indicated is lower than one
     */    
    public GlobalIntegrityRecord(int plevels,int nOfMA) throws BDTreeException, IntegrityRecordException
    {
        if(plevels<1) throw new BDTreeException("The indicated levels must be upper or equal to zero");
        if(nOfMA<1) throw new IntegrityRecordException("The default value for the measurement adapter should be upper or equal than one");
        
        levels=plevels;
        numberOfMA=nOfMA;
        
        map=new ConcurrentHashMap(10);        
    }
    
    /**
     * It initializes the global integrity rercord for the number of transactions indicated (i.e.2^levels)
 and containing the number of project entries in the record.
     * @param plevels It defines the number of transactions to be managed by each MA Integrity Record  (i.e. 2^plevels)
     * @param nOfMA It defines the default value for the number of measurement adapters per project
     * @param nofPrj It defines the number of projects to be contained by project
     * @throws BDTreeException It is raised when the number of levels is lower than 1
     * @throws IntegrityRecordException  It is raised when a) the number of measurement adapters is indicated under 1,
     * or b) the number of projects is lower than one.
     */
    public GlobalIntegrityRecord(int plevels,int nOfMA, int nofPrj) throws BDTreeException, IntegrityRecordException
    {
        if(plevels<1) throw new BDTreeException("The indicated levels must be upper or equal to zero");
        if(nOfMA<1) throw new IntegrityRecordException("The default value for the measurement adapter should be upper or equal than one");
        if(nofPrj<1) throw new IntegrityRecordException("The number of projects should be upper or equal than 1");
            
        levels=plevels;
        numberOfMA=nOfMA;
        
        map=new ConcurrentHashMap(nofPrj);
    }
    
    /**
     * It adds a new hash associated with a transaction in the ProjectIntegrityRecord.In case of the record does not exist, it is created.
     * @param projectID The project ID
     * @param maID The measurement adapter ID
     * @param currentRole The current role related to the measurement adapterr
     * @param hashMD5 The hash associated with the transaction to be stored
     * @return TRUE when the transaction has been stored in the integrity record of the measurement adapter in the project, FALSE otherwise
     * @throws BDTreeException It is raised when the Merkel tree cannot be created
     * @throws NoSuchAlgorithmException It is raised when the MD5 algorithm is not available on the platform
     * @throws TreeNodeException It is raised when the new node for the transaction cannot be created
     * @throws org.ciedayap.mair.IntegrityRecordException  It is raised when the number of measurement adapter records in the hash map is indicated under 1.
     */
    public synchronized Boolean addTransaction(String projectID, String maID,short currentRole, String hashMD5) throws BDTreeException, NoSuchAlgorithmException, TreeNodeException, IntegrityRecordException
    {
        if(projectID==null || projectID.trim().length()==0) return false;
        if(maID==null || maID.trim().length()==0) return false;
        if(!MAIntegrityRecord.isValidRole(currentRole)) return false;
        if(hashMD5==null || hashMD5.trim().length()==0) return false;
        
        ProjectIntegrityRecord record=(map.containsKey(projectID))?map.get(projectID):new ProjectIntegrityRecord(this.levels,this.numberOfMA);
        
        boolean ret= record.addTransaction(maID, currentRole, hashMD5);
        map.put(projectID, record);
        
        return ret;
    }
    
    /**
     * it verifies whether the root hash matches or not with the indicated hash in the indicated measurement adapter
     * @param projectID The project ID related to the measurement adapter
     * @param maID It indicates the measurement adapter in which the verification should be made
     * @param comeMD5 The hash to be contrasted with the root hash
     * @return TRUE when the hashes match between them, FALSE otherwise
     */    
    public synchronized Boolean hasWholeIntegrity(String projectID, String maID,String comeMD5)
    {
        if(maID==null || maID.trim().length()==0) return false;
        if(projectID==null || projectID.trim().length()==0) return false;
        if(comeMD5==null || comeMD5.trim().length()==0) return false;
        
        ProjectIntegrityRecord record=map.get(projectID);
        if(record==null) return false;
                
        return record.hasWholeIntegrity(maID,comeMD5);
    }
    
    /**
     * It verifies the hash of the firsts 2^levels transactions.
     * @param projectID The project ID associated with the measurement adapter
     * @param maID It indicates the measurement adapter in which the verification should be made
     * @param hashMD5 The hash associated with the firsts 2^levels transactions
     * @param levels The number of levels related to the transactions to be contrasted. 
     * For example, if levels=1 then it will imply that the firsts 2^1=2 transactions correspond with
     * the indicated hashMD5 and it will be contrasted with the information on the tree.
     * @return TRUE when the hashes match between them, FALSE otherwise
     */    
    public synchronized Boolean verifyIntegrityFirsts(String projectID, String maID,String hashMD5, int levels)
    {
        if(maID==null || maID.trim().length()==0) return false;
        if(projectID==null || projectID.trim().length()==0) return false;
        if(levels<1) return false;
        if(hashMD5==null || hashMD5.trim().length()==0) return false;
        
        ProjectIntegrityRecord record=map.get(projectID);
        if(record==null) return false;
                
        return record.verifyIntegrityFirsts(maID,hashMD5, levels);
    }
    
    /**
     * It verifies the hash of the lasts 2^levels transactions.
     * @param projectID The project ID associated with the measurement adapter
     * @param maID It indicates the measurement adapter in which the verification should be made
     * @param hashMD5 The hash associated with the lasts 2^levels transactions
     * @param levels The number of levels related to the transactions to be contrasted. 
     * For example, if levels=1 then it will imply that the lasts 2^1=2 transactions correspond with
     * the indicated hashMD5 and it will be contrasted with the information on the tree.
     * @return TRUE when the hashes match between them, FALSE otherwise
     */        
    public synchronized Boolean verifyIntegrityLasts(String projectID,String maID, String hashMD5, int levels)
    {
        if(maID==null || maID.trim().length()==0) return false;
        if(projectID==null || projectID.trim().length()==0) return false;
        if(levels<1) return false;
        if(hashMD5==null || hashMD5.trim().length()==0) return false;
        
        ProjectIntegrityRecord record=map.get(projectID);
        if(record==null) return false;
                
        return record.verifyIntegrityLasts(maID,hashMD5, levels);
    }
    
    /**
     * It verify the hash for a given transaction.The transactions are identified
        considering the relative offset (i.e.1 is the oldest and 2^levels (of the tree)
        is the newest.
     * @param projectID The project ID associated with the measurement adapter
     * @param maID It indicates the measurement adapter in which the verification should be made
     * @param hashMD5 The hash to be verified
     * @param offset The offset of the transaction for whom the hash is associated
     * @return TRUE when the hashes match, false otherwise
     */    
    public synchronized Boolean verifyTransactionIntegrity(String projectID,String maID, String hashMD5, int offset)
    {
        if(maID==null || maID.trim().length()==0) return false;
        if(projectID==null || projectID.trim().length()==0) return false;
        if(hashMD5==null || hashMD5.trim().length()==0) return false;
        if(offset<1) return false;
        
        ProjectIntegrityRecord record=map.get(projectID);
        if(record==null) return false;
                
        return record.verifyTransactionIntegrity(maID,hashMD5, offset);
    }    
    
    /**
     * It returns a string version of the merkle tree associated with the indicated MA
     * @param projectID The project id associated with the measurement adapter
     * @param maID The measurement adapter to be analyzed
     * @return A string representation of the merkle tree for the indicated measurement adapter 
     */
    public String show(String projectID, String maID)
    {
        if(map==null) return null;

        ProjectIntegrityRecord record=map.get(projectID);
        if(record==null) return null;
        
        return record.show(maID);
    }    
    
    public static void main(String args[]) throws BDTreeException, IntegrityRecordException, NoSuchAlgorithmException, TreeNodeException
    {
        int maxprj=5;
        int maxma=5;
        int maxlevels=4;
        GlobalIntegrityRecord gir=new GlobalIntegrityRecord(maxlevels,maxma,maxprj);
        
        for(int prj=1;prj<=maxprj;prj++)
        {
            for(int ma=1;ma<=maxma;ma++)
            {
                for(int i=1;i<=((int)Math.pow(2, maxlevels));i++)
                gir.addTransaction(String.valueOf(prj), String.valueOf(ma), MAIntegrityRecord.ROLE_DATA_COLLECTOR, prj+"."+ma+"."+i);
            }
        }
        
        
        System.out.println(gir.show("2", "4"));
    }
}
