/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.io.FilenameUtils
 */
package com.labvantage.sapphire.util.file;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FilenameUtils;

public class FileTransferOptions {
    private boolean replaceTarget = false;
    private boolean deleteSourceOnSuccessfullTransfer = false;
    private boolean flattenFileStructure = false;
    private boolean validateHashValue = false;
    private boolean retrunHashValue = false;
    private boolean validateFileSize = false;
    private boolean includeSubFolder = true;
    private boolean closeoutputstream = false;
    private boolean closeinputstream = false;
    private boolean encryptdata = false;
    private boolean decryptdata = false;
    private int forceDeleteTargetRetryCount = 1;
    private int generateZipRetryCount = 1;
    private int transferRetryCount = 1;
    private int timeoutSecond = 0;
    private int compressionlevel = 1;
    private long fileSizeFrom = 0L;
    private long fileSizeTo = 0L;
    private long sourceHashValue = 0L;
    private long targetHashValue = 0L;
    private List<String> fileIncldueList = new ArrayList<String>();
    private List<String> fileExcludeList = new ArrayList<String>();
    private CopyOptions copyoption = CopyOptions.NIO_Channel_transferTO;
    private HashingAlgorithm defaultHashingAlgorithm = HashingAlgorithm.MD5;
    private HashingAlgorithm hashingAlgorithm = HashingAlgorithm.None;
    private int DEFAULT_BUFFER_SIZE;
    private int BUFFER_SIZE = this.DEFAULT_BUFFER_SIZE = 8192;
    private String encryptDecryptPassword = "$apphire@pa$$w0rd";
    private String encryptDecryptAlgorithm = "";
    private String defaultEncryptDecryptAlgorithm = "PBEWITHMD5ANDDES";
    private Map<String, Integer> hm = new HashMap<String, Integer>();

    public void setBufferSize(int buffersize) {
        this.BUFFER_SIZE = buffersize;
    }

    public int getBufferSize() {
        return this.BUFFER_SIZE;
    }

    public void setHashingAlgorithm(HashingAlgorithm hashingAlgorithm) {
        this.hashingAlgorithm = hashingAlgorithm;
    }

    public HashingAlgorithm getHashingAlgorithm() {
        return this.hashingAlgorithm;
    }

    public HashingAlgorithm getDefaultHashingAlgorithm() {
        return this.defaultHashingAlgorithm;
    }

    public void setCompressionLevel(int compressionlevel) {
        this.compressionlevel = compressionlevel;
    }

    public int getCompressionLevel() {
        return this.compressionlevel;
    }

    public void setCopyOption(CopyOptions option) {
        this.copyoption = option;
    }

    public CopyOptions getCopyOption() {
        return this.copyoption;
    }

    public String getLatestFileName(String filename) {
        int version = this.hm.containsKey(filename) ? this.hm.get(filename) + 1 : 0;
        this.hm.put(filename, version);
        String finalname = filename;
        if (version > 0) {
            finalname = FilenameUtils.removeExtension((String)filename) + "_" + version + "." + FilenameUtils.getExtension((String)filename);
        }
        return finalname;
    }

    public void setReplaceTarget(boolean replaceTarget) {
        this.replaceTarget = replaceTarget;
    }

    public boolean replaceTarget() {
        return this.replaceTarget;
    }

    public void setDeleteSourceOnSuccessfullTransfer(boolean deleteSourceOnSuccessfullTransfer) {
        this.deleteSourceOnSuccessfullTransfer = deleteSourceOnSuccessfullTransfer;
    }

    public boolean deleteSourceOnSuccessfullTransfer() {
        return this.deleteSourceOnSuccessfullTransfer;
    }

    public void setFlattenFileStructure(boolean flattenFileStructure) {
        this.flattenFileStructure = flattenFileStructure;
    }

    public boolean isFlattenFileStructure() {
        return this.flattenFileStructure;
    }

    public void setValidateHashValue(boolean validateHashValue) {
        this.validateHashValue = validateHashValue;
    }

    public boolean validateHashValue() {
        return this.validateHashValue;
    }

    public void setReturnHashValue(boolean retrunHashValue) {
        this.retrunHashValue = retrunHashValue;
    }

    public boolean isReturnHashValue() {
        return this.retrunHashValue;
    }

    public void setValidateFileSize(boolean validateFileSize) {
        this.validateFileSize = validateFileSize;
    }

    public boolean validateFileSize() {
        return this.validateFileSize;
    }

    public void setForceDeleteTargetRetryCount(int forceDeleteTargetRetryCount) {
        this.forceDeleteTargetRetryCount = forceDeleteTargetRetryCount;
    }

    public int getForceDeleteTargetRetryCount() {
        return this.forceDeleteTargetRetryCount;
    }

    public void setGenerateZipRetryCount(int generateZipRetryCount) {
        this.generateZipRetryCount = generateZipRetryCount;
    }

    public int getGenerateZipRetryCount() {
        return this.generateZipRetryCount;
    }

    public void setTransferRetryCount(int transferRetryCount) {
        this.transferRetryCount = transferRetryCount;
    }

    public int getTransferRetryCount() {
        return this.transferRetryCount;
    }

    public void setTimeoutSecond(int timeoutSecond) {
        this.timeoutSecond = timeoutSecond;
    }

    public int getTimeoutSecond() {
        return this.timeoutSecond;
    }

    public void setSourceFileSize(long fileSizeFrom) {
        this.fileSizeFrom = fileSizeFrom;
    }

    public long getSourceFileSize() {
        return this.fileSizeFrom;
    }

    public void setTransferredFileSize(long fileSizeTo) {
        this.fileSizeTo = fileSizeTo;
    }

    public long getTransferredFileSize() {
        return this.fileSizeTo;
    }

    public void setSourceHashValue(long sourceHashValue) {
        this.sourceHashValue = sourceHashValue;
    }

    public long getSourceHashValue() {
        return this.sourceHashValue;
    }

    public void setTargetHashValue(long targetHashValue) {
        this.targetHashValue = targetHashValue;
    }

    public long getTargetHashValue() {
        return this.targetHashValue;
    }

    public void addFileInclude(String fileInclude) {
        this.fileIncldueList.add(fileInclude);
    }

    public List getFileIncludeList() {
        return this.fileIncldueList;
    }

    public void addFileExclude(String fileExclude) {
        this.fileExcludeList.add(fileExclude);
    }

    public List getFileExcludeList() {
        return this.fileExcludeList;
    }

    public void setIncludeSubFolder(boolean includeSubFolder) {
        this.includeSubFolder = includeSubFolder;
    }

    public boolean includeSubFolder() {
        return this.includeSubFolder;
    }

    public void setCloseOutputStream(boolean closeoutputstream) {
        this.closeoutputstream = closeoutputstream;
    }

    public boolean closeOutputStream() {
        return this.closeoutputstream;
    }

    public void setCloseInputStream(boolean closeinputstream) {
        this.closeinputstream = closeinputstream;
    }

    public boolean closeInputStream() {
        return this.closeinputstream;
    }

    public void setEncryptData(boolean encryptdata) {
        this.encryptdata = encryptdata;
    }

    public boolean encryptData() {
        return this.encryptdata;
    }

    public void setDecryptData(boolean decryptdata) {
        this.decryptdata = decryptdata;
    }

    public boolean decryptData() {
        return this.decryptdata;
    }

    public void setEncryptDecryptPassword(String password) {
        this.encryptDecryptPassword = password;
    }

    public String getEncryptDecryptPassword() {
        return this.encryptDecryptPassword;
    }

    public void setEncryptDecryptAlgorithm(String algorithm) {
        this.encryptDecryptAlgorithm = algorithm;
    }

    public String getEncryptDecryptAlgorithm() {
        return this.encryptDecryptAlgorithm;
    }

    public String getDefaultEncryptDecryptAlgorithm() {
        return this.defaultEncryptDecryptAlgorithm;
    }

    public static enum HashingAlgorithm {
        CRC32,
        MD5,
        SHA1,
        SHA256,
        SHA384,
        SHA512,
        None;

    }

    public static enum CopyOptions {
        NIO_Channel_transferTO,
        NIO_Channel_transferFrom,
        ApacheCommonsIO_FileUtils_CopyFile,
        NIO_Files_copy,
        FileInputStream,
        NIO_Files_Move,
        IO_File_renameTO,
        FileUtils_moveFile;

    }
}

