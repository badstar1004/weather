package zerobase.weather.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zerobase.weather.domain.DateWeather;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DateWeatherRepository extends JpaRepository<DateWeather, LocalDate> {

    /**
     * 조회 (날짜 기준)
     *
     * @param date
     * @return
     */
    List<DateWeather> findAllByDate(LocalDate date);


}
