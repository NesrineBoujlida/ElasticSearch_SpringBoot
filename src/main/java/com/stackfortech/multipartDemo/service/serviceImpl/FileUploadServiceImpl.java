package com.stackfortech.multipartDemo.service.serviceImpl;

import com.spire.doc.Document;
import com.stackfortech.multipartDemo.search.SearchRequestDTO;
import com.stackfortech.multipartDemo.search.util.SearchUtil;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import com.stackfortech.multipartDemo.helper.Indices;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stackfortech.multipartDemo.model.Documents;
import com.stackfortech.multipartDemo.model.UploadedFile;
import com.stackfortech.multipartDemo.repository.FileUploadRepository;

import com.stackfortech.multipartDemo.service.FileUploadService;

import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import java.util.*;
import java.util.Date;




@Service
public class FileUploadServiceImpl implements FileUploadService {

    private String uploadFolderPath = "/Users/HP/Desktop/file38";
    @Autowired
    private FileUploadRepository fileUploadRepository;


    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Logger LOG = LoggerFactory.getLogger(FileUploadServiceImpl.class);

    private final RestHighLevelClient client;

    public FileUploadServiceImpl(RestHighLevelClient client) {
        this.client = client;
    }

    @Override
    public void uploadToLocal(MultipartFile file) {

        try {
            byte[] data = file.getBytes();
            Path path = Paths.get(uploadFolderPath + file.getOriginalFilename());
            Files.write(path, data);
            // byte[] to string
            String s = new String(data, StandardCharsets.UTF_8);
            System.out.println(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
        @Override
        public UploadedFile uploadToDb(MultipartFile file) {
            return null;
        }
*/
    public List<Documents> search(final SearchRequestDTO dto) {
        final SearchRequest request = SearchUtil.buildSearchRequest(
                Indices.Documents_INDEX,
                dto
        );

        return searchInternal(request,dto);
    }

    /**
     * Used to get all vehicles that have been created since forwarded date.
     *
     * @param date Date that is forwarded to the search.
     * @return Returns all vehicles created since forwarded date.
     */
    public List<Documents> getAllVehiclesCreatedSince(SearchRequestDTO dto,final Date date) {
        final SearchRequest request = SearchUtil.buildSearchRequest(
                Indices.Documents_INDEX,
                "created",
                date
        );

        return searchInternal(request,dto);
    }

    public List<Documents> searchCreatedSince(final SearchRequestDTO dto, final Date date) {
        final SearchRequest request = SearchUtil.buildSearchRequest(
                Indices.Documents_INDEX,
                dto,
                date
        );

        return searchInternal(request,dto);
    }

    private List<Documents> searchInternal(final SearchRequest request,SearchRequestDTO dto) {
        if (request == null) {
            LOG.error("Failed to build search request");
            return Collections.emptyList();
        }

        try {
            final SearchResponse response = client.search(request, RequestOptions.DEFAULT);

            final SearchHit[] searchHits = response.getHits().getHits();
            final List<Documents> documents = new ArrayList<>(searchHits.length);

            for (SearchHit hit : searchHits) {

                documents.add(
                        MAPPER.readValue(hit.getSourceAsString(), Documents.class)

                );
            }





            return documents;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public UploadedFile downloadFile(String fileId) {
        return null;
    }

    @Override
    public Documents uploadToDb(MultipartFile file) throws IOException, InvalidFormatException {



           Date date = new Date();
            byte[] data = file.getBytes();
            Path path = Paths.get(uploadFolderPath + file.getOriginalFilename());
            Files.write(path, data);
            String content = file.getContentType();
          //  File MyFile = new File(String.valueOf(path));
            FileInputStream MyFile = new FileInputStream(String.valueOf(path));

            // byte[] to string
            String s = new String(data, StandardCharsets.UTF_8);
        System.out.println(file.getContentType());
            Documents documents = new Documents();
        documents.setId(UUID.randomUUID().toString());
        documents.setType(file.getContentType());
        documents.setName(file.getOriginalFilename());

        if (content.equals("text/plain")){

            documents.setContent(s);
        }
        else if (content.equals("application/pdf"))
               {

                   PDDocument pdDocument = PDDocument.load(MyFile);
                   PDFTextStripper pdfStripper =  new PDFTextStripper();
                   String text = pdfStripper.getText(pdDocument);
                   documents.setContent(text);

                 }
        else if (content.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")){


            //Load Word document
            Document document = new Document();
            document.loadFromFile(String.valueOf(path));

            //Get text from document as string
            String mytext=document.getText();
            documents.setContent(mytext);

        }

        else {
            documents.setContent(String.valueOf(data));
        }
        documents.setFilePath(String.valueOf(path));
        documents.setCreated(date);

        final String DocumentsAsString = MAPPER.writeValueAsString(documents);

        final IndexRequest request = new IndexRequest(Indices.Documents_INDEX);
        request.id(documents.getId());
        request.source(DocumentsAsString, XContentType.JSON);

        final IndexResponse response = client.index(request, RequestOptions.DEFAULT);



        return documents;

    }


    public Documents getById(final String documentsId) {
        try {
            final GetResponse documentFields = client.get(
                    new GetRequest(Indices.Documents_INDEX, documentsId),
                    RequestOptions.DEFAULT
            );
            if (documentFields == null || documentFields.isSourceEmpty()) {
                return null;
            }

            return MAPPER.readValue(documentFields.getSourceAsString(), Documents.class);
        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
            return null;
        }
    }



}
      
      

    /*@Override
    public UploadedFile downloadFile(String fileId) {
        UploadedFile uploadedFileToRet = fileUploadRepository.getOne(fileId);
        return uploadedFileToRet;
    }
*/
