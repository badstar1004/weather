package zerobase.weather.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import zerobase.weather.domain.Memo;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcMemoRepository {
    private final JdbcTemplate jdbcTemplate;        // jdbc 정보

    /**
     * 생성자
     */
    @Autowired
    public JdbcMemoRepository(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /**
     * 저장
     */
    public Memo save(Memo memo) {
        String sql = "INSERT INTO MEMO VALUES(?, ?);";
        jdbcTemplate.update(sql, memo.getId(), memo.getText());

        return memo;
    }

    /***
     *  전체 조회
     */
    public List<Memo> findAll() {
        String sql = "SELECT * FROM MEMO;";

        return jdbcTemplate.query(sql, memoRowMapper());
    }

    /***
     *  아이디 기준 조회
     */
    public Optional<Memo> findById(int id) {
        String sql = "SELECT * FROM MEMO WHERE ID = ?;";

        return jdbcTemplate.query(sql, memoRowMapper(), id).stream().findFirst();
    }

    /**
     * 조회 시 맵핑
     */
    private RowMapper<Memo> memoRowMapper() {
        /*  RowMapper    맵핑을 해줌
            ResultSet
            {id = 1, text = 'this is memo~'}
        */
        // rs = ResultSet rowNum으로 Memo 객체 반환
        return ((rs, rowNum) -> new Memo(
                rs.getInt("id"),
                rs.getString("text")
        ));
    }
}
