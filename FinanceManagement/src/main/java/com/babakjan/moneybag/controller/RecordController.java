package com.babakjan.moneybag.controller;

import com.babakjan.moneybag.dto.record.CreateRecordRequest;
import com.babakjan.moneybag.dto.record.RecordDto;
import com.babakjan.moneybag.dto.record.UpdateRecordRequest;
import com.babakjan.moneybag.entity.ErrorMessage;
import com.babakjan.moneybag.entity.Record;
import com.babakjan.moneybag.error.exception.AccountNotFoundException;
import com.babakjan.moneybag.error.exception.CategoryNotFoundException;
import com.babakjan.moneybag.error.exception.RecordNotFoundException;
import com.babakjan.moneybag.error.exception.UserNotFoundException;
import com.babakjan.moneybag.service.RecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.kaczmarzyk.spring.data.jpa.domain.*;
import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/records", produces = "application/json")
@Tag(name = "Record", description = "Record of financial transaction")
@SecurityRequirement(name = "bearer-key")
@ApiResponses(value = {
        @ApiResponse(
                responseCode = "403",
                description = "Forbidden. Role USER tries to access or manipulate not their data.",
                content = @Content
        ),
        @ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Authentication is required.",
                content = @Content
        ),
        @ApiResponse(
                responseCode = "400",
                description = "Bad request",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))
        )
})
public class RecordController {

    private final RecordService recordService;

    /**
     * Get record by id. Role ADMIN can access all records, role USER only theirs.
     * @param id record id
     * @return record of specified id
     * @throws RecordNotFoundException Record of specified id doesn't exist.
     * @throws UserNotFoundException Authenticated user doesn't exist.
     */
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Return record by id.",
            description = "Role ADMIN can access all records, role USER only theirs"
    )
    public RecordDto getById(@PathVariable Long id) throws RecordNotFoundException, UserNotFoundException {
        return recordService.getById(id).dto();
    }

    /**
     * Get all records.
     * @param pageable pagination specification (page, size, sort,...)
     * @param userId user id
     * @param specification filter parameters
     * @return page of filtered records
     * @throws UserNotFoundException Authenticated user doesn't exist.
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Return all records.",
            description = "Role ADMIN can access all records, role USER only records from their accounts."
    )
    public Page<RecordDto> getAllFilter(Pageable pageable, @RequestParam(required = false) Long userId, @And({
            @Spec( path = "label", params = "label", spec = LikeIgnoreCase.class),
            @Spec( path = "note", params = "note", spec = LikeIgnoreCase.class),
            @Spec( path = "date", params = { "dateGe", "dateLt" }, spec = Between.class),
            @Spec( path = "account.id", params = "accountId", spec = Equal.class),
            @Spec( path = "category.id", params = "categoryId", spec = Equal.class),
            @Spec( path = "account.user.id", params = "userId", spec = Equal.class),
            @Spec( path = "amount", params = "amountLt", spec = LessThan.class),
            @Spec( path = "amount", params = "amountGt", spec = GreaterThan.class),
    }) Specification<Record> specification)
            throws UserNotFoundException {
        return recordService.getAllFilter(specification, pageable, userId).map(Record::dto);
    }

    /**
     * Create new record. Role ADMIN can create records to all accounts, role USER only to their accounts.
     * @param request record data
     * @return created record
     * @throws CategoryNotFoundException Category from request doesn't exist.
     * @throws AccountNotFoundException Account from request doesn't exist.
     * @throws UserNotFoundException Authenticated user of user from request doesn't exist.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create new record.",
            description = "Role ADMIN can create records to all accounts, role USER only to their accounts."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "404",
                    description = "Category or account not found.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessage.class)
                    )
            )
    })
    public RecordDto create(@RequestBody @Valid CreateRecordRequest request)
            throws CategoryNotFoundException, AccountNotFoundException, UserNotFoundException {
        return recordService.save(request).dto();
    }

    /**
     * Delete record by id. Role ADMIN can delete all records, role USER only records from their accounts.
     * @param id record id
     * @throws RecordNotFoundException Record of specified id doesn't exist.
     * @throws UserNotFoundException Authenticated user doesn't exist.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Delete record by id.",
            description = "Role ADMIN can delete all records, role USER only records from their accounts."
    )
    public void deleteById(@PathVariable Long id) throws RecordNotFoundException, UserNotFoundException {
        recordService.deleteById(id);
    }

    /**
     * Update record by id. Role ADMIN can update all records, role USER can update only records from their accounts.
     * @param id record id
     * @param request record data (only fields, which will be changed)
     * @return updated account
     * @throws RecordNotFoundException Record of specified id doesn't exist.
     * @throws CategoryNotFoundException Category from request doesn't exist.
     * @throws AccountNotFoundException Account from request doesn't exist.
     * @throws UserNotFoundException Authenticated user of user from request doesn't exist.
     */
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Update record by id.",
            description = "Update existing record by id, null or not provided fields are ignored. Role ADMIN can " +
                    "update all records, role USER can update only records from their accounts."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated."),
            @ApiResponse(
                responseCode = "404",
                description = "Record, category or account not found.",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))
            )
    })
    public RecordDto update(@PathVariable Long id, @RequestBody @Valid UpdateRecordRequest request)
            throws RecordNotFoundException, CategoryNotFoundException, AccountNotFoundException, UserNotFoundException {
        return recordService.update(id, request).dto();
    }
}
