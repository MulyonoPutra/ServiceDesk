package com.labs.servicedesk.controller;

import com.labs.servicedesk.domain.entity.Category;
import com.labs.servicedesk.exception.BadRequestAlertException;
import com.labs.servicedesk.repository.CategoryRepository;
import com.labs.servicedesk.service.CategoryService;
import com.labs.servicedesk.utils.HeaderUtils;
import com.labs.servicedesk.utils.PaginationUtil;
import com.labs.servicedesk.utils.ResponseUtil;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * REST controller for managing {@link com.labs.servicedesk.domain.entity.Category}.
 */
@RestController
@RequestMapping("/api")
public class CategoryController {

  private final Logger log = LoggerFactory.getLogger(CategoryController.class);

  private static final String ENTITY_NAME = "category";

  private final CategoryService categoryService;

  private final CategoryRepository categoryRepository;

  public CategoryController(
    CategoryService categoryService,
    CategoryRepository categoryRepository
  ) {
    this.categoryService = categoryService;
    this.categoryRepository = categoryRepository;
  }

  /**
   * {@code POST  /categories} : Create a new category.
   *
   * @param category the category to create.
   * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new category, or with status {@code 400 (Bad Request)} if the category has already an ID.
   * @throws URISyntaxException if the Location URI syntax is incorrect.
   */
  @PostMapping("/categories")
  public ResponseEntity<Category> createCategory(
    @Valid @RequestBody Category category
  )
    throws URISyntaxException, BadRequestAlertException {
    log.debug("REST request to save Category : {}", category);
    if (category.getId() != null) {
      throw new BadRequestAlertException(
        "A new category cannot already have an ID"
      );
    }
    Category result = categoryService.save(category);
    return ResponseEntity
      .created(new URI("/api/categories/" + result.getId()))
      .headers(
        HeaderUtils.createEntityCreationAlert(
          false,
          ENTITY_NAME,
          result.getId().toString()
        )
      )
      .body(result);
  }

  /**
   * {@code PUT  /categories/:id} : Updates an existing category.
   *
   * @param id the id of the category to save.
   * @param category the category to update.
   * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated category,
   * or with status {@code 400 (Bad Request)} if the category is not valid,
   * or with status {@code 500 (Internal Server Error)} if the category couldn't be updated.
   * @throws BadRequestAlertException if the Location URI syntax is incorrect.
   */
  @PutMapping("/categories/{id}")
  public ResponseEntity<Category> updateCategory(
    @PathVariable(value = "id", required = false) final Long id,
    @Valid @RequestBody Category category
  )
    throws BadRequestAlertException {
    log.debug("REST request to update Category : {}, {}", id, category);
    if (category.getId() == null) {
      throw new BadRequestAlertException("Invalid id");
    }
    if (!Objects.equals(id, category.getId())) {
      throw new BadRequestAlertException("Invalid ID");
    }

    if (!categoryRepository.existsById(id)) {
      throw new BadRequestAlertException("Entity not found");
    }

    Category result = categoryService.save(category);
    return ResponseEntity
      .ok()
      .headers(
        HeaderUtils.createEntityUpdateAlert(
          false,
          ENTITY_NAME,
          category.getId().toString()
        )
      )
      .body(result);
  }

  /**
   * {@code PATCH  /categories/:id} : Partial updates given fields of an existing category, field will ignore if it is null
   *
   * @param id the id of the category to save.
   * @param category the category to update.
   * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated category,
   * or with status {@code 400 (Bad Request)} if the category is not valid,
   * or with status {@code 404 (Not Found)} if the category is not found,
   * or with status {@code 500 (Internal Server Error)} if the category couldn't be updated.
   * @throws URISyntaxException if the Location URI syntax is incorrect.
   */
  @PatchMapping(
    value = "/categories/{id}",
    consumes = "application/merge-patch+json"
  )
  public ResponseEntity<Category> partialUpdateCategory(
    @PathVariable(value = "id", required = false) final Long id,
    @NotNull @RequestBody Category category
  )
    throws URISyntaxException, BadRequestAlertException {
    log.debug(
      "REST request to partial update Category partially : {}, {}",
      id,
      category
    );
    if (category.getId() == null) {
      throw new BadRequestAlertException("Invalid id");
    }
    if (!Objects.equals(id, category.getId())) {
      throw new BadRequestAlertException("Invalid ID");
    }

    if (!categoryRepository.existsById(id)) {
      throw new BadRequestAlertException("Entity not found");
    }

    Optional<Category> result = categoryService.partialUpdate(category);

    return ResponseUtil.wrapOrNotFound(
      result,
      HeaderUtils.createEntityUpdateAlert(
        false,
        ENTITY_NAME,
        category.getId().toString()
      )
    );
  }

  /**
   * {@code GET  /categories} : get all the categories.
   *
   * @param pageable the pagination information.
   * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of categories in body.
   */
  @GetMapping("/categories")
  public ResponseEntity<List<Category>> getAllCategories(Pageable pageable) {
    log.debug("REST request to get a page of Categories");
    Page<Category> page = categoryService.findAll(pageable);
    HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
      ServletUriComponentsBuilder.fromCurrentRequest(),
      page
    );
    return ResponseEntity.ok().headers(headers).body(page.getContent());
  }

  /**
   * {@code GET  /categories/:id} : get the "id" category.
   *
   * @param id the id of the category to retrieve.
   * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the category, or with status {@code 404 (Not Found)}.
   */
  @GetMapping("/categories/{id}")
  public ResponseEntity<Category> getCategory(@PathVariable Long id) {
    log.debug("REST request to get Category : {}", id);
    Optional<Category> category = categoryService.findOne(id);
    return ResponseUtil.wrapOrNotFound(category);
  }

  /**
   * {@code DELETE  /categories/:id} : delete the "id" category.
   *
   * @param id the id of the category to delete.
   * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
   */
  @DeleteMapping("/categories/{id}")
  public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
    log.debug("REST request to delete Category : {}", id);
    categoryService.delete(id);
    return ResponseEntity
      .noContent()
      .headers(
        HeaderUtils.createEntityDeletionAlert(false, ENTITY_NAME, id.toString())
      )
      .build();
  }
}
