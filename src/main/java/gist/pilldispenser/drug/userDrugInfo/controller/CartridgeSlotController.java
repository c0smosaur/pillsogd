package gist.pilldispenser.drug.userDrugInfo.controller;

import gist.pilldispenser.common.security.UsersDetails;
import gist.pilldispenser.drug.api.drugIdentificationAPI.domain.entity.DrugIdentification;
import gist.pilldispenser.drug.api.drugIdentificationAPI.repository.DrugIdentificationRepository;
import gist.pilldispenser.drug.userDrugInfo.domain.dto.CartridgeDiskSizeResponse;
import gist.pilldispenser.drug.userDrugInfo.domain.dto.CartridgeSlotResponseDto;
import gist.pilldispenser.drug.userDrugInfo.service.CartridgeSlotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/cartridge-slots")
@RequiredArgsConstructor
public class CartridgeSlotController {

    private final CartridgeSlotService cartridgeSlotService;
    private final DrugIdentificationRepository drugIdentificationRepository;

    @Operation(summary = "유저가 약을 넣어야 하는 카트리지 번호를 반환", description = "유저에게 남은 카트리지 중 약을 담을 카트리지 번호를 반환합니다.")
    @GetMapping("/assign")
    public ResponseEntity<CartridgeSlotResponseDto> findLowestCartridgeNo(
            @AuthenticationPrincipal UsersDetails usersDetails) {
        CartridgeSlotResponseDto response = cartridgeSlotService.findLowestAvailableSlotId(usersDetails.getId());
        return ResponseEntity.ok(response);
    }

    //
    @Operation(summary = "DB에서 알약 일련번호를 조회하여 디스크 사이즈를 반환", description = "해당 알약에 맞는 디스크 사이즈를 찾아 반환합니다.")
    @GetMapping("/disk/item-seq")
    public ResponseEntity<CartridgeDiskSizeResponse> assignDiskByItemSeq(
            @Parameter(description = "유저 ID") @AuthenticationPrincipal UsersDetails usersDetails,
            @Parameter(description = "알약 고유번호") @RequestParam(required = false) String itemSeq) {

        if (itemSeq != null) {
            DrugIdentification drugIdentification = drugIdentificationRepository.findByItemSeq(itemSeq)
                    .orElseThrow(()->new RuntimeException("no such entry"));
            String availableSlot = cartridgeSlotService.assignDiskByItemSeq(usersDetails.getId(), itemSeq);
            CartridgeDiskSizeResponse response = CartridgeDiskSizeResponse.builder()
                    .diskSize(availableSlot)
                    .drugShape(drugIdentification.getDrugShape())
                    .build();

            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            throw new RuntimeException("약을 찾을 수 없습니다.");
        }
    }

    @Operation(summary = "유저가 등록한 약에 맞는 디스크 사이즈를 반환", description = "유저가 등록한 알약에 맞는 디스크사이즈를 찾아 반환합니다")
    @GetMapping("/disk/user-drug")
    public ResponseEntity<String> assignDiskByUserDrug(
            @Parameter(description = "사용자 정보") @AuthenticationPrincipal UsersDetails usersDetails,
            @Parameter(description = "알약 모양") @RequestParam String drugShape,
            @Parameter(description = "알약 크기") @RequestParam Double drugLeng) {

        String availableSlot = cartridgeSlotService.assignDiskByDrugInfoId(
                usersDetails.getId(), drugShape, drugLeng);
        return new ResponseEntity<>(availableSlot, HttpStatus.OK);
    }

    @DeleteMapping("/reset")
    public ResponseEntity<String> resetCartridge(
            @Parameter(description = "사용자 정보") @AuthenticationPrincipal UsersDetails usersDetails){

        try {
            cartridgeSlotService.resetCartridgeSlots(usersDetails);
            return ResponseEntity.ok("카트리지 리셋");
        } catch (Exception e) {
            log.info("카트리지 리셋에 실패했습니다.");
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

//    @DeleteMapping("/remove/{cartridgeId}")
//    public ResponseEntity<String> removeSingleCartridge(
//            @Parameter(description = "사용자 정보") @AuthenticationPrincipal UsersDetails usersDetails
//    )
}