package com.forever.dadamda.service;

import static com.forever.dadamda.entity.board.TAG.LIFE_SHOPPING;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import com.forever.dadamda.dto.board.CreateBoardRequest;
import com.forever.dadamda.dto.board.GetBoardContentsResponse;
import com.forever.dadamda.dto.board.GetBoardResponse;
import com.forever.dadamda.dto.board.UpdateBoardContentsRequest;
import com.forever.dadamda.dto.board.UpdateBoardRequest;
import com.forever.dadamda.entity.board.Board;
import com.forever.dadamda.entity.user.User;
import com.forever.dadamda.exception.NotFoundException;
import com.forever.dadamda.repository.board.BoardRepository;
import com.forever.dadamda.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = "/truncate.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
@Sql(scripts = "/board-setup.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
public class BoardServiceTest {

    @Autowired
    private BoardService boardService;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private UserRepository userRepository;

    String existentEmail = "1234@naver.com";

    UUID board2UUID = UUID.fromString("30373832-6566-3438-2d61-3433392d3132");

    UUID board1UUID = UUID.fromString("30373832-6566-3438-2d61-3433392d3131");

    @Test
    void should_the_description_and_heart_count_are_generated_normally_When_creating_the_board() {
        // 새로운 보드가 정상적으로 이름, 설명, 하트수가 생성되는지 확인
        //given
        boardRepository.deleteAll();

        CreateBoardRequest createBoardRequest = CreateBoardRequest.builder()
                .tag("ENTERTAINMENT_ART")
                .title("board normally test1")
                .description("board test2")
                .build();

        User user = userRepository.findByEmailAndDeletedDateIsNull(existentEmail).get();

        //when
        boardService.createBoards(existentEmail, createBoardRequest);

        //then
        Optional<Board> board = boardRepository.findByUserAndTitle(user, "board normally test1");
        assertThat(board).isNotNull();
        assertThat(board.get().getDescription()).isEqualTo("board test2");
        assertThat(board.get().getHeartCnt()).isEqualTo(0L);
    }

    @Test
    void should_the_deleted_date_is_not_null_When_deleting_a_board() {
        // 보드를 삭제했을 때, 삭제된 날짜가 null이 아닌지 확인
        //given
        //when
        boardService.deleteBoards(existentEmail, 1L);

        //then
        assertThat(boardRepository.findById(1L).get().getDeletedDate()).isNotNull();
    }

    @Test
    void should_the_fixed_date_is_before_or_equal_to_now_When_fixing_a_board() {
        // 보드를 고정했을 때, 고정된 날짜가 null이 아닌지(보드가 정상적으로 고정되는지) 확인
        //given
        //when
        boardService.fixedBoards(existentEmail, 1L);

        //then
        assertThat(boardRepository.findById(1L).get().getFixedDate()).isBeforeOrEqualTo(
                LocalDateTime.now());
    }

    @Test
    void should_the_fixed_date_is_null_When_fixing_a_fixed_board() {
        // 고정된 보드를 다시 고정할 때, 고정된 날짜가 null이 되는지(고정이 취소되는지) 확인
        //given
        //when
        boardService.fixedBoards(existentEmail, 2L);

        //then
        assertThat(boardRepository.findById(2L).get().getFixedDate()).isNull();
    }

    @Test
    void should_boards_are_sorted_by_fixed_date_and_modified_date_order_When_getting_a_list_of_boards() {
        // 보드 목록을 조회할 때, 고정된 날짜, 수정된 날짜 순으로 정렬되는지 확인
        //given
        PageRequest pageRequest = PageRequest.of(0, 10);

        //when
        Slice<GetBoardResponse> getBoardResponseSlice = boardService.getBoardList(existentEmail,
                pageRequest);

        //then
        assertThat(getBoardResponseSlice.getContent().get(0).getBoardId()).isEqualTo(4L);
        assertThat(getBoardResponseSlice.getContent().get(1).getBoardId()).isEqualTo(2L);
        assertThat(getBoardResponseSlice.getContent().get(2).getBoardId()).isEqualTo(3L);
        assertThat(getBoardResponseSlice.getContent().get(3).getBoardId()).isEqualTo(1L);
    }

    @Test
    void should_the_tag_name_description_modified_date_are_modified_successfully_When_modifying_a_board() {
        // 보드를 수정할 때, tag, name, description, modified_date가 성공적으로 수정되는지 확인
        //given
        Long boardId = 1L;
        UpdateBoardRequest updateBoardRequest = UpdateBoardRequest.builder()
                .tag("LIFE_SHOPPING")
                .title("test")
                .description("test123")
                .build();

        //when
        boardService.updateBoards(existentEmail, boardId, updateBoardRequest);

        //then
        Board board = boardRepository.findById(boardId).get();

        assertThat(board.getModifiedDate()).isAfter(
                LocalDateTime.of(2023, 1, 2, 11, 11, 1));
        assertThat(board.getTag()).isEqualTo(LIFE_SHOPPING);
        assertThat(board.getTitle()).isEqualTo("test");
        assertThat(board.getDescription()).isEqualTo("test123");
    }

    @Test
    void should_it_is_not_modified_it_is_the_same_as_the_previous_one_When_modifying_a_board() {
        // 보드를 수정할 때, 이전 내용과 동일하다면, 수정되지 않는지 확인
        //given
        Long boardId = 1L;
        UpdateBoardRequest updateBoardRequest = UpdateBoardRequest.builder()
                .tag("ENTERTAINMENT_ART")
                .title("board1")
                .description("test")
                .build();

        //when
        boardService.updateBoards(existentEmail, boardId, updateBoardRequest);

        //then
        Board board = boardRepository.findById(boardId).get();

        assertThat(board.getModifiedDate()).isEqualTo(
                LocalDateTime.of(2023, 1, 1, 11, 11, 1));

    }

    @Test
    void should_the_number_of_boards_is_returned_successfully_except_for_deleted_ones_When_getting_the_number_of_boards() {
        // 보드 개수 조회할 때, 삭제된 보드는 제외하고 개수가 정상적으로 나오는지 확인
        //given
        //when
        Long boardsCount = boardService.getBoardCount(existentEmail);

        //then
        assertThat(boardsCount).isEqualTo(4L);
    }

    @Test
    void should_it_occurs_not_found_exception_When_getting_deleted_board() {
        // 삭제된 보드 조회할 때, NotFoundException(NOT_EXISTS_BOARD) 예외가 발생하는지 확인
        //given
        Long deletedBoardId = 5L;

        //when
        //then
        assertThatThrownBy(() -> boardService.getBoard(existentEmail, deletedBoardId))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void should_deleted_boards_are_not_searched_When_searching_for_a_board() {
        // 보드를 검색할 때, 삭제된 보드는 검색 안 된다.
        //given
        String keyword = "board5";

        //when
        Slice<GetBoardResponse> getBoardResponseSlice = boardService.searchBoards(existentEmail,
                keyword, PageRequest.of(0, 10));

        //then
        assertThat(getBoardResponseSlice.getNumberOfElements()).isEqualTo(0);
    }

    @Test
    void should_boards_are_sorted_by_fixed_date_and_modified_date_order_When_searching_for_a_board() {
        // 보드를 검색할 때, 고정된 날짜, 수정된 날짜 순으로 정렬되는지 확인
        //given
        String keyword = "board";

        //when
        Slice<GetBoardResponse> getBoardResponseSlice = boardService.searchBoards(existentEmail,
                keyword, PageRequest.of(0, 10));

        //then
        assertThat(getBoardResponseSlice.getContent().get(0).getBoardId()).isEqualTo(4L);
        assertThat(getBoardResponseSlice.getContent().get(1).getBoardId()).isEqualTo(2L);
        assertThat(getBoardResponseSlice.getContent().get(2).getBoardId()).isEqualTo(3L);
        assertThat(getBoardResponseSlice.getContent().get(3).getBoardId()).isEqualTo(1L);
    }

    @Test
    void should_the_board_contents_are_actually_modified_When_modifying_board_contents() {
        // 보드 컨텐츠 수정할 때, 보드의 컨텐츠가 실제로 수정되었는지 확인
        //given
        UpdateBoardContentsRequest updateBoardContentsRequest = UpdateBoardContentsRequest.builder()
                .contents("update test")
                .build();

        //when
        boardService.updateBoardContents(existentEmail, board2UUID, updateBoardContentsRequest);

        //then
        User user = userRepository.findById(1L).get();
        Board board = boardRepository.findByUserAndUuidAndDeletedDateIsNull(user, board2UUID).get();

        assertThat(board.getContents()).isEqualTo("update test");
    }

    @Test
    void should_it_is_not_modified_if_it_is_the_same_as_the_previous_content_When_modifying_board_contents() {
        // 보드 컨텐츠 수정할 때, 이전 컨텐츠와 동일하면 수정되지 않는다.
        //given
        UpdateBoardContentsRequest updateBoardContentsRequest = UpdateBoardContentsRequest.builder()
                .contents("test contents")
                .build();

        //when
        boardService.updateBoardContents(existentEmail, board2UUID, updateBoardContentsRequest);

        //then
        User user = userRepository.findById(1L).get();
        Board board = boardRepository.findByUserAndUuidAndDeletedDateIsNull(user, board2UUID).get();

        assertThat(board.getModifiedDate()).isEqualTo(LocalDateTime.of(2023, 1, 2, 11, 11, 1));
    }

    @Test
    void should_it_is_returned_to_null_if_it_is_not_present_When_getting_board_contents() {
        // 보드 컨텐츠 조회할 때, 컨텐츠가 없으면 null로 반환되는지 확인
        //given
        //when
        GetBoardContentsResponse getBoardContentsResponse = boardService.getBoardContents(existentEmail, board1UUID);

        //then
        assertThat(getBoardContentsResponse.getContents()).isEqualTo(null);
    }
}
