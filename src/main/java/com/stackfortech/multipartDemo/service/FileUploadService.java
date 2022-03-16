package com.stackfortech.multipartDemo.service;

import com.stackfortech.multipartDemo.model.Documents;
import com.stackfortech.multipartDemo.model.UploadedFile;
import com.stackfortech.multipartDemo.search.SearchRequestDTO;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface FileUploadService {

    public void uploadToLocal(MultipartFile file);
    public Documents uploadToDb(MultipartFile file) throws IOException, InvalidFormatException;
    public UploadedFile downloadFile(String fileId);
    public List<Documents> search(final SearchRequestDTO dto);
    public Documents getById(final String documentsId);
}
