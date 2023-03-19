package zerobase.weather.service;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import zerobase.weather.WeatherApplication;
import zerobase.weather.domain.DateWeather;
import zerobase.weather.domain.Diary;
import zerobase.weather.repository.DateWeatherRepository;
import zerobase.weather.repository.DiaryRepository;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class DiaryService {

    @Value("${openweathermap.key}")
    private String apiKey;

    private final DiaryRepository diaryRepository;

    private final DateWeatherRepository dateWeatherRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(WeatherApplication.class);

    /***
     * 생성자
     * @param diaryRepository
     * @param dateWeatherRepository
     */
    public DiaryService(DiaryRepository diaryRepository, DateWeatherRepository dateWeatherRepository) {
        this.diaryRepository = diaryRepository;
        this.dateWeatherRepository = dateWeatherRepository;
    }

    /***
     * 저장
     * @param date
     * @param text
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void createDiary(LocalDate date, String text) {
        LOGGER.info("Started to Create Diary.");

        // 스케쥴링으로 인해 AIP로 호출로 데이터가 저장되기 때문에 API 구문은 제거
        // 날씨 데이터 가져오기 (DB 데이터)
        DateWeather dateWeather = getDateWeather(date);

        // 파싱된 데이터 + 일기 값 db에 저장
        Diary nowDiary = new Diary();
        nowDiary.setDateWeather(dateWeather);
        nowDiary.setText(text);

        diaryRepository.save(nowDiary);

        LOGGER.info("End to Create Diary.");
    }

    /**
     * 데이터 가져오기
     *
     * @param date
     * @return
     */
    private DateWeather getDateWeather(LocalDate date) {
        List<DateWeather> dateWeatherListFromDB = dateWeatherRepository.findAllByDate(date);

        if (dateWeatherListFromDB.isEmpty()) {
            // 새로 api에서 날씨 정보를 가져온다
            // 정책상 현재 날씨를 가져오거나, 날씨 없이 일기 작성
            // 현재 날씨를 가져와 저장하도록 구현
            return getWeatherFromApi();
        } else {
            return dateWeatherListFromDB.get(0);
        }
    }

    /***
     * 데이터 받아오기
     * @return
     */
    private String getWeatherString() {
        String apiUrl = "https://api.openweathermap.org/data/2.5/weather?q=seoul&appid="
                + apiKey;

        try {
            // 오류가 발생할 수 있어 try로
            URL url = new URL(apiUrl);

            // url 연결
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");

            // 응답결과 코드
            int responseCode = httpURLConnection.getResponseCode();

            BufferedReader br;

            if (responseCode == 200) {
                br = new BufferedReader(new InputStreamReader(
                        httpURLConnection.getInputStream()));
            } else {
                br = new BufferedReader(new InputStreamReader(
                        httpURLConnection.getErrorStream()));
            }

            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }

            return response.toString();

        } catch (Exception e) {
            return "failed to get response";
        }
    }

    /***
     * JSON Parse
     * @param jsonString
     * @return
     */
    private Map<String, Object> parseWeather(String jsonString) {
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject;

        try {
            jsonObject = (JSONObject) jsonParser.parse(jsonString);

        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        // HashMap
        Map<String, Object> resultMap = new HashMap<>();

        JSONArray weatherArray = (JSONArray) jsonObject.get("weather");
        JSONObject weatherData = (JSONObject) weatherArray.get(0);
        resultMap.put("main", weatherData.get("main"));
        resultMap.put("icon", weatherData.get("icon"));

        JSONObject mainData = (JSONObject) jsonObject.get("main");
        resultMap.put("temp", mainData.get("temp"));

        return resultMap;
    }

    /**
     * 조회 (날짜 기준)
     *
     * @param date
     * @return
     */
    @Transactional(readOnly = true)
    public List<Diary> readDiary(LocalDate date) {
        LOGGER.debug("Read Diary.");
        return diaryRepository.findAllByDate(date);
    }

    /**
     * 조회 (날짜 범위 기준)
     *
     * @param startDate
     * @param endDate
     * @return
     */
    @Transactional(readOnly = true)
    public List<Diary> readDiaries(LocalDate startDate, LocalDate endDate) {
        return diaryRepository.findAllByDateBetween(startDate, endDate);
    }

    /**
     * 수정
     *
     * @param date
     * @param text
     */
    public void updateDiary(LocalDate date, String text) {
        // 첫번째 일기만 수정
        Diary nowDiary = diaryRepository.getFirstByDate(date);

        // 일기값만 수정
        nowDiary.setText(text);

        // merge insert/update 처리됨
        diaryRepository.save(nowDiary);
    }

    /***
     * 삭제 (날짜 기준)
     * @param date
     */
    public void deleteDiary(LocalDate date) {
        diaryRepository.deleteAllByDate(date);
    }

    /**
     * 스케쥴링
     */
    @Transactional
    @Scheduled(cron = "0 0 1 * * *")        // 매일 새벽 1시에 스케쥴링
    public void saveWeatherDate() {
        dateWeatherRepository.save(getWeatherFromApi());
    }

    /**
     * 스케쥴링을 위한 API 호출
     *
     * @return
     */
    private DateWeather getWeatherFromApi() {
        // 데이터 받아오기
        String weatherData = getWeatherString();

        // 받아온 날씨 json 파싱
        Map<String, Object> parsedWeather = parseWeather(weatherData);

        DateWeather dateWeather = new DateWeather();
        dateWeather.setDate(LocalDate.now());
        dateWeather.setWeather(parsedWeather.get("main").toString());
        dateWeather.setIcon(parsedWeather.get("icon").toString());
        dateWeather.setTemperature((Double) parsedWeather.get("temp"));

        return dateWeather;
    }
}
