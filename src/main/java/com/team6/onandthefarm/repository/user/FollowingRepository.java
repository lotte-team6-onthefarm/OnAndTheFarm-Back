package com.team6.onandthefarm.repository.user;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.team6.onandthefarm.entity.user.Following;

@Repository
public interface FollowingRepository extends CrudRepository<Following, Long> {
	Optional<Following> findByFollowingMemberIdAndFollowerMemberId(
			Long followingMemberId, Long followerMemberId);

	Optional<Following> findByFollowingMemberIdAndFollowingMemberRole(
			Long followingMemberId, String followerMemberRole);

	@Query("select f from Following f where f.followerMemberId =:followerId")
	List<Following> findFollowingIdByFollowerId(@Param("followerId")Long followerId);

	@Query("select f from Following f where f.followingMemberId =:followingId")
	List<Following> findFollowerIdByFollowingId(@Param("followingId")Long followingId);

	@Query("select f from Following f where f.followingMemberId=:memberId")
	List<Following> findByFollowingMemberId(@Param("memberId") Long memberId);
}

