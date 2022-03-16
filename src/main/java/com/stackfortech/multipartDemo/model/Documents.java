package com.stackfortech.multipartDemo.model;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import javax.annotation.sql.DataSourceDefinition;
import javax.persistence.GeneratedValue;



public class Documents  {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid",strategy = "uuid2")

   
    private String id;

    
    private String name;


    private String type;

    
    private String content;

  
    private String filePath;

    public Documents() {
    }

    public Documents(String id, String name, String type, String content, String filePath) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.content = content;
        this.filePath = filePath;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public  void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public  void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
