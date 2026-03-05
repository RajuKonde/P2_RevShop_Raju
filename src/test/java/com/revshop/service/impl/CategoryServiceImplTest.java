package com.revshop.service.impl;

import com.revshop.dao.CategoryDAO;
import com.revshop.dao.ProductDAO;
import com.revshop.dao.UserDAO;
import com.revshop.dto.category.CategoryCreateRequest;
import com.revshop.dto.category.CategoryResponse;
import com.revshop.dto.category.CategoryTreeResponse;
import com.revshop.entity.Category;
import com.revshop.entity.Role;
import com.revshop.entity.User;
import com.revshop.exception.BadRequestException;
import com.revshop.exception.ForbiddenOperationException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CategoryServiceImplTest {

    @Mock
    private CategoryDAO categoryDAO;

    @Mock
    private ProductDAO productDAO;

    @Mock
    private UserDAO userDAO;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    public void createCategory_normalizesFieldsAndPersistsNewCategory() {
        CategoryCreateRequest request = new CategoryCreateRequest();
        request.setName("  Home   Decor  ");
        request.setDescription("  Stylish   accents   ");

        User seller = User.builder()
                .email("seller@test.com")
                .role(Role.SELLER)
                .active(true)
                .build();

        when(userDAO.findByEmail("seller@test.com")).thenReturn(Optional.of(seller));
        when(categoryDAO.findAnyByName("Home Decor")).thenReturn(Optional.empty());
        when(categoryDAO.findBySlug("home-decor")).thenReturn(Optional.empty());
        when(categoryDAO.save(any(Category.class))).thenAnswer(invocation -> {
            Category toSave = invocation.getArgument(0);
            toSave.setId(21L);
            return toSave;
        });

        CategoryResponse response = categoryService.createCategory("seller@test.com", request);

        ArgumentCaptor<Category> categoryCaptor = ArgumentCaptor.forClass(Category.class);
        verify(categoryDAO).save(categoryCaptor.capture());
        Category savedCategory = categoryCaptor.getValue();

        assertEquals("Home Decor", savedCategory.getName());
        assertEquals("home-decor", savedCategory.getSlug());
        assertEquals("Stylish accents", savedCategory.getDescription());
        assertTrue(savedCategory.getActive());
        assertEquals(Long.valueOf(21L), response.getId());
        assertEquals("Home Decor", response.getName());
    }

    @Test
    public void createCategory_throwsWhenAuthenticatedUserIsNotSeller() {
        CategoryCreateRequest request = new CategoryCreateRequest();
        request.setName("Books");

        User buyer = User.builder()
                .email("buyer@test.com")
                .role(Role.BUYER)
                .active(true)
                .build();

        when(userDAO.findByEmail("buyer@test.com")).thenReturn(Optional.of(buyer));

        assertThrows(ForbiddenOperationException.class, () -> categoryService.createCategory("buyer@test.com", request));
    }

    @Test
    public void deleteCategory_marksCategoryInactiveWhenItHasNoDependencies() {
        User seller = User.builder()
                .email("seller@test.com")
                .role(Role.SELLER)
                .active(true)
                .build();

        Category category = Category.builder()
                .id(31L)
                .name("Beauty")
                .active(true)
                .build();
        category.setIsDeleted(false);

        when(userDAO.findByEmail("seller@test.com")).thenReturn(Optional.of(seller));
        when(categoryDAO.findById(31L)).thenReturn(Optional.of(category));
        when(productDAO.countActiveByCategoryId(31L)).thenReturn(0L);
        when(categoryDAO.countActiveChildren(31L)).thenReturn(0L);

        categoryService.deleteCategory(31L, "seller@test.com");

        assertFalse(category.getActive());
        assertTrue(category.getIsDeleted());
        verify(categoryDAO).save(category);
    }

    @Test
    public void deleteCategory_throwsWhenCategoryStillHasActiveProducts() {
        User seller = User.builder()
                .email("seller@test.com")
                .role(Role.SELLER)
                .active(true)
                .build();

        Category category = Category.builder()
                .id(41L)
                .name("Appliances")
                .active(true)
                .build();

        when(userDAO.findByEmail("seller@test.com")).thenReturn(Optional.of(seller));
        when(categoryDAO.findById(41L)).thenReturn(Optional.of(category));
        when(productDAO.countActiveByCategoryId(41L)).thenReturn(3L);

        assertThrows(BadRequestException.class, () -> categoryService.deleteCategory(41L, "seller@test.com"));
    }

    @Test
    public void getCategoryTree_groupsChildrenUnderTheirVisibleParent() {
        Category parent = Category.builder()
                .id(1L)
                .name("Electronics")
                .description("Devices")
                .active(true)
                .build();
        parent.setIsDeleted(false);

        Category child = Category.builder()
                .id(2L)
                .name("Audio")
                .description("Speakers")
                .parent(parent)
                .active(true)
                .build();
        child.setIsDeleted(false);

        when(categoryDAO.findAllActiveWithParent()).thenReturn(List.of(parent, child));

        List<CategoryTreeResponse> tree = categoryService.getCategoryTree();

        assertEquals(1, tree.size());
        CategoryTreeResponse root = tree.get(0);
        assertEquals(Long.valueOf(1L), root.getId());
        assertNotNull(root.getChildren());
        assertEquals(1, root.getChildren().size());
        assertEquals(Long.valueOf(2L), root.getChildren().get(0).getId());
        assertTrue(root.getChildren().get(0).getChildren().isEmpty());
    }
}
