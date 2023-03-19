package zerobase.weather.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import zerobase.weather.domain.Diary;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DiaryRepository extends JpaRepository<Diary, Integer> {

    /**
     * 조회 (날짜 기준)
     *
     * @param date
     * @return
     */
    List<Diary> findAllByDate(LocalDate date);

    /**
     * 조회 (날짜 범위 기준)
     *
     * @param startDate
     * @param endDate
     * @return
     */
    List<Diary> findAllByDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * 첫번째 일기만 수정
     *
     * @param date
     * @return
     */
    Diary getFirstByDate(LocalDate date);

    /**
     * 삭제 (날짜 기준)
     *
     * @param date
     */
    @Transactional
    void deleteAllByDate(LocalDate date);
}
