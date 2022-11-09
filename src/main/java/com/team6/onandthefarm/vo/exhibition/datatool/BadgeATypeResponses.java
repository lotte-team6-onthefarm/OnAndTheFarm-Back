package com.team6.onandthefarm.vo.exhibition.datatool;

import java.util.List;

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
public class BadgeATypeResponses implements BadgeResponses{
	List<BadgeATypeResponse> badgeATypeResponseList;
}
