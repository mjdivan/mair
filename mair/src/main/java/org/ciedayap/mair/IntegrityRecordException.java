/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ciedayap.mair;

/**
 * It specializes the Exception class to be used with the Integrity Records.
 * 
 * @author Mario Diván
 * @version 1.0
 */
public class IntegrityRecordException extends Exception {
   public IntegrityRecordException(String mess)
   {
       super(mess);
   }
}
