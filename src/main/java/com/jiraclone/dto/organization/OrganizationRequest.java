package com.jiraclone.dto.organization;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrganizationRequest {
    @NotBlank(message = "Tên tổ chức không được để trống")
    private String orgName;
    @NotBlank(message = "Mã tổ chức không được để trống")
    private String slug;
    @NotBlank(message = "Trạng thái không được để trống")
    private String status;
    private String description;
}
