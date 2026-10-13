package com.example.graduation_java_game;

import java.util.List;

/**
 * 1問に対するプレイヤーの回答です。
 *
 * @param stageQuestionId 回答するStageQuestionのID
 * @param selectedChoiceIds 選択したChoiceのID。時間切れは空リスト
 */
public record AnswerRequest(Long stageQuestionId, List<Long> selectedChoiceIds) {
}
