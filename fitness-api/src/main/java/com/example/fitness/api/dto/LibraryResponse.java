package com.example.fitness.api.dto;

import lombok.Data;
import java.util.List;

@Data
public class LibraryResponse {
    private List<MoveDTO> moves;
    private List<Object> sessions; // Using Object for now as Session DTO might not exist
}
