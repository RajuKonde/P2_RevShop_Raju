package com.revshop.controller;

import com.revshop.dto.category.CategoryCreateRequest;
import com.revshop.dto.category.CategoryResponse;
import com.revshop.dto.common.ApiResponse;
import com.revshop.service.CategoryService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CategoryControllerTest {

    @Mock
    private CategoryService categoryService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CategoryController categoryController;

    @Test
    public void createCategory_returnsWrappedSuccessResponse() {
        CategoryCreateRequest request = new CategoryCreateRequest();
        request.setName("Electronics");
        request.setDescription("Devices and accessories");

        CategoryResponse categoryResponse = CategoryResponse.builder()
                .id(9L)
                .name("Electronics")
                .description("Devices and accessories")
                .build();

        when(authentication.getName()).thenReturn("seller@test.com");
        when(categoryService.createCategory("seller@test.com", request)).thenReturn(categoryResponse);

        ResponseEntity<ApiResponse<CategoryResponse>> response = categoryController.createCategory(request, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Category created successfully", response.getBody().getMessage());
        assertEquals(Long.valueOf(9L), response.getBody().getData().getId());
        verify(categoryService).createCategory("seller@test.com", request);
    }

    @Test
    public void deleteCategory_invokesServiceAndReturnsEmptyPayload() {
        when(authentication.getName()).thenReturn("seller@test.com");

        ResponseEntity<ApiResponse<Void>> response = categoryController.deleteCategory(14L, authentication);

        verify(categoryService).deleteCategory(14L, "seller@test.com");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Category deleted successfully", response.getBody().getMessage());
        assertNull(response.getBody().getData());
    }
}
