package qna.domain;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import qna.CannotDeleteException;

public class QuestionTest {

  public static final Question Q1 = new Question("title1", "contents1")
      .writeBy(UserTest.JAVAJIGI);
  public static final Question Q2 = new Question("title2", "contents2")
      .writeBy(UserTest.SANJIGI);
  public static final Question QUESTION_WITH_ANSWER_SAME_WRITER =
      new Question("title3", "contents3")
          .writeBy(UserTest.JAVAJIGI);
  public static final Question QUESTION_WITH_ANSWER_DIFF_WRITER =
      new Question("title4", "contents4")
          .writeBy(UserTest.JAVAJIGI);


  static {
    QUESTION_WITH_ANSWER_SAME_WRITER.addAnswer(AnswerTest.A1);
    QUESTION_WITH_ANSWER_DIFF_WRITER.addAnswer(AnswerTest.A2);
  }


  @ParameterizedTest
  @MethodSource("provideQuestionWithLoginUser")
  void delete_성공(Question question, User loginUser) throws Exception {
    question.delete(loginUser);

    assertThat(question.isDeleted()).isTrue();
  }

  static Stream<Arguments> provideQuestionWithLoginUser() {
    return Stream.of(
        arguments(
            Q1,
            UserTest.JAVAJIGI
        ),
        arguments(
            Q2,
            UserTest.SANJIGI
        ),
        arguments(
            QUESTION_WITH_ANSWER_SAME_WRITER,
            UserTest.JAVAJIGI
        )
    );
  }

  @ParameterizedTest
  @MethodSource("provideQuestionWithInvalidLoginUser")
  void delete_실패(Question question, User loginUser) throws Exception {

    assertThatExceptionOfType(CannotDeleteException.class).isThrownBy(() -> {
      question.delete(loginUser);
    });
  }

  static Stream<Arguments> provideQuestionWithInvalidLoginUser() {
    return Stream.of(
        arguments(
            Q1,
            UserTest.SANJIGI
        ),
        arguments(
            Q2,
            UserTest.JAVAJIGI
        )
    );
  }

  @Test
  void delete_실패_다른사람의_답변존재() throws Exception {
    Question question = QUESTION_WITH_ANSWER_DIFF_WRITER;

    assertThatExceptionOfType(CannotDeleteException.class).isThrownBy(() -> {
      question.delete(UserTest.JAVAJIGI);
    }).withMessage("다른 사람이 쓴 답변이 있어 삭제할 수 없습니다.");
  }

  @ParameterizedTest
  @MethodSource("provideQuestionWithLoginUserToGetDeleteHistories")
  void getDeleteHistories(Question question, User loginUser, List<DeleteHistory> expected)
      throws Exception {
    question.delete(loginUser);

    assertThat(question.getDeleteHistories(loginUser)).isEqualTo(expected);
  }

  static Stream<Arguments> provideQuestionWithLoginUserToGetDeleteHistories() {
    return Stream.of(
        arguments(
            Q1,
            UserTest.JAVAJIGI,
            Arrays.asList(
                new DeleteHistory(ContentType.QUESTION, Q1.getId(), Q1.getWriter(), null)
            )
        ),
        arguments(
            Q2,
            UserTest.SANJIGI,
            Arrays.asList(
                new DeleteHistory(ContentType.QUESTION, Q2.getId(), Q2.getWriter(), null)
            )
        ),
        arguments(
            QUESTION_WITH_ANSWER_SAME_WRITER,
            UserTest.JAVAJIGI,
            Arrays.asList(
                new DeleteHistory(
                    ContentType.QUESTION,
                    QUESTION_WITH_ANSWER_SAME_WRITER.getId(),
                    UserTest.JAVAJIGI,
                    null),
                new DeleteHistory(
                    ContentType.ANSWER,
                    AnswerTest.A1.getId(),
                    UserTest.JAVAJIGI,
                    null)
            )
        )
    );
  }
}
