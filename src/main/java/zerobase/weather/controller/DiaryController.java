package zerobase.weather.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import zerobase.weather.domain.Diary;
import zerobase.weather.service.DiaryService;

import java.time.LocalDate;
import java.util.List;

@RestController
public class DiaryController {

    private final DiaryService diaryService;

    /***
     * 생성자
     * @param diaryService
     */
    public DiaryController(DiaryService diaryService) {
        this.diaryService = diaryService;
    }

    /***
     * 테이블 생성 (저장)
     * @param date
     * @param text
     */
    @ApiOperation(value = "일기 텍스트와 날씨를 이용해서 일기를 저장합니다.")       // 한줄 설명
    @PostMapping("/create/diary")
    void createDiary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @ApiParam(value = "생성할 날짜", example = "2023-03-17")
            LocalDate date,
            @RequestBody String text) {
        // 서비스에 전달해야함
        diaryService.createDiary(date, text);
    }

    /**
     * 조회 (날짜 기준)
     *
     * @param date
     * @return
     */
    @ApiOperation("선택한 날짜의 모든 일기 데이터를 가져옵니다.")
    @GetMapping("/read/diary")
    List<Diary> readDiary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @ApiParam(value = "조회할 날짜", example = "2023-03-17")
            LocalDate date) {
        return diaryService.readDiary(date);
    }

    /**
     * 조회 (날짜 범위 기준)
     *
     * @param startDate
     * @param endDate
     * @return
     */
    @ApiOperation("선택한 기간 중의 모든 일기 데이터를 가져옵니다.")
    @GetMapping("/read/diaries")
    List<Diary> readDiaries(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @ApiParam(value = "조회기간 첫번째날", example = "2023-03-17")
            LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @ApiParam(value = "조회기간 마지막날", example = "2023-03-17")
            LocalDate endDate
    ) {
        return diaryService.readDiaries(startDate, endDate);
    }

    /**
     * 수정
     *
     * @param date
     * @param text
     */
    @ApiOperation("선택한 날짜의 일기를 수정합니다.")
    @PutMapping("/update/diary")
    void updateDiary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @ApiParam(value = "수정할 날짜", example = "2023-03-17")
            LocalDate date,
            @RequestBody String text) {
        diaryService.updateDiary(date, text);
    }

    /**
     * 삭제 (날짜 기준)
     *
     * @param date
     */
    @ApiOperation("선택한 날짜의 일기를 삭제합니다.")
    @DeleteMapping("/delete/diary")
    void deleteDiary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @ApiParam(value = "삭제할 날짜", example = "2023-03-17")
            LocalDate date) {
        diaryService.deleteDiary(date);
    }
}
