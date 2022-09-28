package com.team6.onandthefarm.vo.review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewLikeCancelFormRequest {
	private Long reviewId;
	private Long userId;
}
