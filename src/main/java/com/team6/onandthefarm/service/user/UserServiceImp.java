package com.team6.onandthefarm.service.user;

import com.team6.onandthefarm.dto.user.MemberFollowingDto;
import com.team6.onandthefarm.dto.user.MemberProfileDto;
import com.team6.onandthefarm.dto.user.UserLoginDto;
import com.team6.onandthefarm.dto.user.UserQnaDto;
import com.team6.onandthefarm.dto.user.UserInfoDto;
import com.team6.onandthefarm.dto.user.UserQnaUpdateDto;
import com.team6.onandthefarm.entity.product.Product;
import com.team6.onandthefarm.entity.product.ProductQna;
import com.team6.onandthefarm.entity.seller.Seller;
import com.team6.onandthefarm.entity.user.Following;
import com.team6.onandthefarm.entity.user.User;
import com.team6.onandthefarm.repository.product.ProductQnaAnswerRepository;
import com.team6.onandthefarm.repository.product.ProductQnaRepository;
import com.team6.onandthefarm.repository.product.ProductRepository;
import com.team6.onandthefarm.repository.seller.SellerRepository;
import com.team6.onandthefarm.repository.user.FollowingRepository;
import com.team6.onandthefarm.repository.user.UserRepository;
import com.team6.onandthefarm.security.jwt.JwtTokenUtil;
import com.team6.onandthefarm.security.jwt.Token;
import com.team6.onandthefarm.security.oauth.dto.OAuth2UserDto;
import com.team6.onandthefarm.security.oauth.provider.KakaoOAuth2;
import com.team6.onandthefarm.security.oauth.provider.NaverOAuth2;
import com.team6.onandthefarm.util.DateUtils;
import com.team6.onandthefarm.util.S3Upload;
import com.team6.onandthefarm.vo.product.ProductQnAResultResponse;
import com.team6.onandthefarm.vo.user.*;
import com.team6.onandthefarm.vo.product.ProductQnAResponse;

import lombok.extern.slf4j.Slf4j;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@Slf4j
public class UserServiceImp implements UserService {

	private final int pageContentNumber = 8;

	private final UserRepository userRepository;

	private final SellerRepository sellerRepository;

	private final ProductQnaRepository productQnaRepository;

	private final ProductQnaAnswerRepository productQnaAnswerRepository;

	private final ProductRepository productRepository;

	private final FollowingRepository followingRepository;

	private final KakaoOAuth2 kakaoOAuth2;
	private final NaverOAuth2 naverOAuth2;

	private final JwtTokenUtil jwtTokenUtil;

	private final DateUtils dateUtils;

	private final Environment env;

	private final S3Upload s3Upload;

	@Autowired
	public UserServiceImp(UserRepository userRepository,
			SellerRepository sellerRepository,
			FollowingRepository followingRepository,
			DateUtils dateUtils,
			Environment env,
			ProductQnaRepository productQnaRepository,
			ProductQnaAnswerRepository productQnaAnswerRepository,
			ProductRepository productRepository,
			KakaoOAuth2 kakaoOAuth2,
			NaverOAuth2 naverOAuth2,
			JwtTokenUtil jwtTokenUtil,
			S3Upload s3Upload) {
		this.userRepository = userRepository;
		this.sellerRepository = sellerRepository;
		this.followingRepository = followingRepository;
		this.dateUtils = dateUtils;
		this.env = env;
		this.productQnaRepository = productQnaRepository;
		this.productQnaAnswerRepository=productQnaAnswerRepository;
		this.productRepository = productRepository;
		this.kakaoOAuth2 = kakaoOAuth2;
		this.naverOAuth2 = naverOAuth2;
		this.jwtTokenUtil = jwtTokenUtil;
		this.s3Upload=s3Upload;
	}

	public Boolean createProductQnA(UserQnaDto userQnaDto) {
		Optional<User> user = userRepository.findById(userQnaDto.getUserId());
		Optional<Product> product = productRepository.findById(userQnaDto.getProductId());
		log.info("product 정보  :  " + product.get().toString());
		ProductQna productQna = ProductQna.builder()
				.product(product.get())
				.user(user.get())
				.productQnaContent(userQnaDto.getProductQnaContent())
				.productQnaCreatedAt(dateUtils.transDate(env.getProperty("dateutils.format")))
				.productQnaStatus("waiting")
				.seller(product.get().getSeller())
				.build();
		ProductQna newQna = productQnaRepository.save(productQna);
		if (newQna == null) {
			return Boolean.FALSE;
		}
		return Boolean.TRUE;
	}

	@Override
	public UserTokenResponse login(UserLoginDto userLoginDto) {

		Token token = null;
		Boolean needRegister = false;
		String email = new String();
		Long userId = null;

		String provider = userLoginDto.getProvider();
		if (provider.equals("google")) {

		} else if (provider.equals("naver")) {
			// 카카오 액세스 토큰 받아오기
			String naverAccessToken = naverOAuth2.getAccessToken(userLoginDto);

			if (naverAccessToken != null) {
				// 카카오 액세스 토큰으로 유저 정보 받아오기
				OAuth2UserDto userInfo = naverOAuth2.getUserInfo(naverAccessToken);

				Optional<User> savedUser = userRepository.findByUserEmailAndProvider(userInfo.getEmail(), provider);
				User user = null;

				if (savedUser.isPresent()) {
					user = savedUser.get();

					if (user.getUserName() == null) {
						needRegister = true;
						email = user.getUserEmail();
					}
				} else { // DB에 유저 정보가 없다면 저장
					needRegister = true; // 유저 정보 추가 등록이 필요함
					email = userInfo.getEmail();

					User newUser = User.builder()
							.userEmail(userInfo.getEmail())
							.role("ROLE_USER")
							.provider(provider)
							.userNaverNumber(userInfo.getNaverId())
							.userFollowerCount(0)
							.userFollowingCount(0)
							.userProfileImg("https://lotte-06-s3-test.s3.ap-northeast-2.amazonaws.com/profile/user/basic_profile.png")
							.userRegisterDate(dateUtils.transDate(env.getProperty("dateutils.format")))
							.build();
					user = userRepository.save(newUser);
				}

				// jwt 토큰 발행
				token = jwtTokenUtil.generateToken(user.getUserId(), user.getRole());
				userId = user.getUserId();
			}
		} else if (provider.equals("kakao")) {
			// 카카오 액세스 토큰 받아오기
			String kakaoAccessToken = kakaoOAuth2.getAccessToken(userLoginDto);

			if (kakaoAccessToken != null) {
				// 카카오 액세스 토큰으로 유저 정보 받아오기
				OAuth2UserDto userInfo = kakaoOAuth2.getUserInfo(kakaoAccessToken);

				Optional<User> savedUser = userRepository.findByUserEmailAndProvider(userInfo.getEmail(), provider);

				User user = null;
				if (savedUser.isPresent()) {
					user = savedUser.get();

					if (user.getUserName() == null) {
						needRegister = true;
						email = user.getUserEmail();
					}
				} else { // DB에 유저 정보가 없다면 저장
					needRegister = true; // 유저 정보 추가 등록이 필요함
					email = userInfo.getEmail();

					User newUser = User.builder()
							.userEmail(userInfo.getEmail())
							.role("ROLE_USER")
							.provider(provider)
							.userKakaoNumber(userInfo.getKakaoId())
							.userFollowerCount(0)
							.userFollowingCount(0)
							.userProfileImg("https://lotte-06-s3-test.s3.ap-northeast-2.amazonaws.com/profile/user/basic_profile.png")
							.userRegisterDate(dateUtils.transDate(env.getProperty("dateutils.format")))
							.build();
					user = userRepository.save(newUser);
				}

				// jwt 토큰 발행
				token = jwtTokenUtil.generateToken(user.getUserId(), user.getRole());
				userId = user.getUserId();
			}
		}
		UserTokenResponse userTokenResponse = UserTokenResponse.builder()
				.token(token)
				.needRegister(needRegister)
				.email(email)
				.userId(userId)
				.build();

		return userTokenResponse;
	}

	@Override
	public Boolean logout(Long userId) {
		Optional<User> user = userRepository.findById(userId);

		Long kakaoNumber = user.get().getUserKakaoNumber();
		Long returnKakaoNumber = kakaoOAuth2.logout(kakaoNumber);
		if (returnKakaoNumber == null) {
			return false;
		}

		return true;
	}

	@Override
	public Boolean loginPhoneConfirm(String phone){
		Optional<User> user = userRepository.findByUserPhone(phone);

		if(user.isPresent()){
			return false;
		}

		return true;
	}

	@Override
	public Token reIssueToken(String refreshToken, HttpServletRequest request, HttpServletResponse response) {
		return null;
	}

	@Override
	public Long updateUserInfo(UserInfoDto userInfoDto) throws IOException {
		Optional<User> user = userRepository.findById(userInfoDto.getUserId());

		if(userInfoDto.getProfile()!=null){
			String url = s3Upload.profileUserUpload(userInfoDto.getProfile());
			user.get().setUserProfileImg(url);
		}

		user.get().setUserName(userInfoDto.getUserName());
		user.get().setUserPhone(userInfoDto.getUserPhone());
		user.get().setUserZipcode(userInfoDto.getUserZipcode());
		user.get().setUserAddress(userInfoDto.getUserAddress());
		user.get().setUserAddressDetail(userInfoDto.getUserAddressDetail());
		user.get().setUserBirthday(userInfoDto.getUserBirthday());
		user.get().setUserSex(userInfoDto.getUserSex());


		return user.get().getUserId();
	}

	public ProductQnAResultResponse findUserQna(Long userId, Integer pageNum) {
		ModelMapper modelMapper = new ModelMapper();
		modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

		List<ProductQnAResponse> responses = new ArrayList<>();

		Optional<User> user = userRepository.findById(userId);
		if (user.isPresent()) {
			List<ProductQna> productQnas = productQnaRepository.findByUser(user.get());

			for (ProductQna productQna : productQnas) {
				ProductQnAResponse response = modelMapper.map(productQna, ProductQnAResponse.class);
				if(response.getProductQnaStatus().equals("deleted")) continue;
				if(response.getProductQnaStatus().equals("completed")){
					String answer =
							productQnaAnswerRepository
									.findByProductQna(productQna)
									.getProductQnaAnswerContent();
					response.setProductSellerAnswer(answer);
				}
				response.setUserName(user.get().getUserName());
				response.setUserProfileImg(user.get().getUserProfileImg());
				responses.add(response);
			}
		}

		ProductQnAResultResponse resultResponse = new ProductQnAResultResponse();

		responses.sort((o1, o2) -> {
			int result = o2.getProductQnaCreatedAt().compareTo(o1.getProductQnaCreatedAt());
			return result;
		});

		int startIndex = pageNum*pageContentNumber;

		int size = responses.size();

		if(size<startIndex+pageContentNumber){
			resultResponse.setResponses(responses.subList(startIndex,size));
			resultResponse.setCurrentPageNum(pageNum);
			if(size%pageContentNumber!=0){
				resultResponse.setTotalPageNum((size/pageContentNumber)+1);
			}
			else{
				resultResponse.setTotalPageNum(size/pageContentNumber);
			}
			return resultResponse;
		}

		resultResponse.setResponses(responses.subList(startIndex,startIndex+pageContentNumber));
		resultResponse.setCurrentPageNum(pageNum);
		if(size%pageContentNumber!=0){
			resultResponse.setTotalPageNum((size/pageContentNumber)+1);
		}
		else{
			resultResponse.setTotalPageNum(size/pageContentNumber);
		}
		return resultResponse;
	}

	public UserInfoResponse findUserInfo(Long userId) {
		ModelMapper modelMapper = new ModelMapper();
		modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

		Optional<User> user = userRepository.findById(userId);

		UserInfoResponse response = modelMapper.map(user.get(), UserInfoResponse.class);

		return response;
	}

	/**
	 * 유저의 질의를 수정하는 메서드
	 * @param userQnaUpdateDto
	 * @return
	 */
	public Boolean updateUserQna(UserQnaUpdateDto userQnaUpdateDto) {
		Optional<ProductQna> productQna = productQnaRepository.findById(userQnaUpdateDto.getProductQnaId());
		productQna.get().setProductQnaContent(userQnaUpdateDto.getProductQnaContent());
		productQna.get().setProductQnaModifiedAt(dateUtils.transDate(env.getProperty("dateutils.format")));
		if (productQna.get().getProductQnaContent().equals(userQnaUpdateDto.getProductQnaContent())) {
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}

	public Boolean deleteUserQna(Long productQnaId) {
		Optional<ProductQna> productQna = productQnaRepository.findById(productQnaId);
		productQna.get().setProductQnaStatus("deleted");
		if (productQna.get().getProductQnaStatus().equals("deleted")) {
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}

	@Override
	public Long addFollowList(MemberFollowingDto memberFollowingDto) {
		Long followingMemberId = memberFollowingDto.getFollowingMemberId();
		Long followerMemberId = memberFollowingDto.getFollowerMemberId();
		String followingMemberRole = memberFollowingDto.getFollowingMemberRole();
		String followerMemberRole = memberFollowingDto.getFollowerMemberRole();

		Optional<Following> savedFollowing = followingRepository.findByFollowingMemberIdAndFollowerMemberId(
				followingMemberId, followerMemberId);

		if (savedFollowing.isPresent()) {
			return savedFollowing.get().getFollowingId();
		}

		if (followingMemberRole.equals("user") && followerMemberRole.equals("user")) {
			User followingMember = userRepository.findById(followingMemberId).get();
			User followerMember = userRepository.findById(followerMemberId).get();

			followingMember.setUserFollowingCount(followingMember.getUserFollowingCount() + 1);
			followerMember.setUserFollowerCount(followerMember.getUserFollowerCount() + 1);
		} else if (followingMemberRole.equals("user") && followerMemberRole.equals("seller")) {
			User followingMember = userRepository.findById(followingMemberId).get();
			Seller followerMember = sellerRepository.findById(followerMemberId).get();

			followingMember.setUserFollowingCount(followingMember.getUserFollowingCount() + 1);
			followerMember.setSellerFollowerCount(followerMember.getSellerFollowerCount() + 1);
		} else if (followingMemberRole.equals("seller") && followerMemberRole.equals("user")) {
			Seller followingMember = sellerRepository.findById(followingMemberId).get();
			User followerMember = userRepository.findById(followerMemberId).get();

			followingMember.setSellerFollowingCount(followingMember.getSellerFollowingCount() + 1);
			followerMember.setUserFollowerCount(followerMember.getUserFollowerCount() + 1);
		} else if (followingMemberRole.equals("seller") && followerMemberRole.equals("seller")) {
			Seller followingMember = sellerRepository.findById(followingMemberId).get();
			Seller followerMember = sellerRepository.findById(followerMemberId).get();

			followingMember.setSellerFollowingCount(followingMember.getSellerFollowingCount() + 1);
			followerMember.setSellerFollowerCount(followingMember.getSellerFollowerCount() + 1);
		}

		Following following = Following.builder()
				.followingMemberId(followingMemberId)
				.followingMemberRole(followingMemberRole)
				.followerMemberId(followerMemberId)
				.followerMemberRole(followerMemberRole)
				.build();
		Long followingId = followingRepository.save(following).getFollowingId();

		return followingId;
	}

	@Override
	public Long cancelFollowList(MemberFollowingDto memberFollowingDto) {
		Long followingCancelMemberId = memberFollowingDto.getFollowingMemberId();
		Long followerCancelMemberId = memberFollowingDto.getFollowerMemberId();
		String followingCancelMemberRole = memberFollowingDto.getFollowingMemberRole();
		String followerCancelMemberRole = memberFollowingDto.getFollowerMemberRole();

		if (followingCancelMemberRole.equals("user") && followerCancelMemberRole.equals("user")) {
			User followingMember = userRepository.findById(followingCancelMemberId).get();
			User followerMember = userRepository.findById(followerCancelMemberId).get();

			followingMember.setUserFollowingCount(followingMember.getUserFollowingCount() - 1);
			followerMember.setUserFollowerCount(followerMember.getUserFollowerCount() - 1);
		} else if (followingCancelMemberRole.equals("user") && followerCancelMemberRole.equals("seller")) {
			User followingMember = userRepository.findById(followingCancelMemberId).get();
			Seller followerMember = sellerRepository.findById(followerCancelMemberId).get();

			followingMember.setUserFollowingCount(followingMember.getUserFollowingCount() - 1);
			followerMember.setSellerFollowerCount(followerMember.getSellerFollowerCount() - 1);
		} else if (followingCancelMemberRole.equals("seller") && followerCancelMemberRole.equals("user")) {
			Seller followingMember = sellerRepository.findById(followingCancelMemberId).get();
			User followerMember = userRepository.findById(followerCancelMemberId).get();

			followingMember.setSellerFollowingCount(followingMember.getSellerFollowingCount() - 1);
			followerMember.setUserFollowerCount(followerMember.getUserFollowerCount() - 1);
		} else if (followingCancelMemberRole.equals("seller") && followerCancelMemberRole.equals("seller")) {
			Seller followingMember = sellerRepository.findById(followingCancelMemberId).get();
			Seller followerMember = sellerRepository.findById(followerCancelMemberId).get();

			followingMember.setSellerFollowingCount(followingMember.getSellerFollowingCount() - 1);
			followerMember.setSellerFollowerCount(followingMember.getSellerFollowerCount() - 1);
		}

		Following following = followingRepository.findByFollowingMemberIdAndFollowerMemberId(
				followingCancelMemberId, followerCancelMemberId).get();
		Long followingId = following.getFollowingId();
		followingRepository.delete(following);

		return followingId;
	}

	@Override
	public MemberFollowResult getFollowerList(MemberFollowerListRequest memberFollowerListRequest){

		Long memberId = memberFollowerListRequest.getMemberId();
		Long loginMemberId = memberFollowerListRequest.getLoginMemberId();

		List<Following> followerList = followingRepository.findFollowingIdByFollowerId(memberId);

		int startIndex = memberFollowerListRequest.getPageNumber() * pageContentNumber;
		int size = followerList.size();

		MemberFollowResult memberFollowResult = getResponseForFollower(size, startIndex, followerList, loginMemberId);
		memberFollowResult.setCurrentPageNum(memberFollowerListRequest.getPageNumber());
		memberFollowResult.setTotalElementNum(size);
		if(size%pageContentNumber==0){
			memberFollowResult.setTotalPageNum(size/pageContentNumber);
		}
		else{
			memberFollowResult.setTotalPageNum((size/pageContentNumber)+1);
		}

		return memberFollowResult;
	}

	@Override
	public MemberFollowResult getFollowingList(MemberFollowingListRequest memberFollowingListRequest){

		Long memberId = memberFollowingListRequest.getMemberId();
		Long loginMemberId = memberFollowingListRequest.getLoginMemberId();

		List<Following> followingList = followingRepository.findFollowerIdByFollowingId(memberId);

		int startIndex = memberFollowingListRequest.getPageNumber() * pageContentNumber;
		int size = followingList.size();

		MemberFollowResult memberFollowResult = getResponseForFollowing(size, startIndex, followingList, loginMemberId);

		memberFollowResult.setCurrentPageNum(memberFollowingListRequest.getPageNumber());
		memberFollowResult.setTotalElementNum(size);
		if(size%pageContentNumber==0){
			memberFollowResult.setTotalPageNum(size/pageContentNumber);
		}
		else{
			memberFollowResult.setTotalPageNum((size/pageContentNumber)+1);
		}

		return memberFollowResult;
	}

	public MemberProfileResponse getMemberProfile(MemberProfileDto memberProfileDto){
		Long memberId = memberProfileDto.getMemberId();
		String memberRole = memberProfileDto.getMemberRole();

		MemberProfileResponse memberProfileResponse = null;
		if(memberRole.equals("user")){
			User user = userRepository.findById(memberId).get();

			String userName = user.getUserName();
			String userProfileImage = user.getUserProfileImg();

			memberProfileResponse = MemberProfileResponse.builder()
					.memberName(userName)
					.memberProfileImage(userProfileImage)
					.followingCount(user.getUserFollowingCount())
					.followerCount(user.getUserFollowerCount())
					.build();
		}
		else if (memberRole.equals("seller")){
			Seller seller = sellerRepository.findById(memberId).get();
			memberProfileResponse = MemberProfileResponse.builder()
					.memberName(seller.getSellerName())
					.memberProfileImage(seller.getSellerProfileImg())
					.followingCount(seller.getSellerFollowingCount())
					.followerCount(seller.getSellerFollowerCount())
					.build();
		}

		memberProfileResponse.setFollowStatus(false);
		if(memberId.equals(memberProfileDto.getLoginMemberId())){
			memberProfileResponse.setIsModifiable(true);
		}
		else{
			memberProfileResponse.setIsModifiable(false);
			Optional<Following> following = followingRepository.findByFollowingMemberIdAndFollowerMemberId(memberProfileDto.getLoginMemberId(), memberId);
			if(following.isPresent()){
				memberProfileResponse.setFollowStatus(true);
			}
		}

		return memberProfileResponse;
	}

	public MemberFollowResult getResponseForFollower(int size, int startIndex, List<Following> followerList, Long loginMemberId){
		MemberFollowResult memberFollowResult = new MemberFollowResult();
		List<MemberFollowListResponse> responseList = new ArrayList<>();
		if(size < startIndex){
			memberFollowResult.setMemberFollowListResponseList(responseList);
			return memberFollowResult;
		}

		if(size < startIndex + pageContentNumber) {
			for (Following following : followerList.subList(startIndex, size)) {
				Long followingMemberId = following.getFollowingMemberId();
				String followingMemberRole = following.getFollowingMemberRole();

				MemberFollowListResponse memberFollowListResponse = new MemberFollowListResponse();
				if(followingMemberRole.equals("user")){
					User user = userRepository.findById(followingMemberId).get();
					memberFollowListResponse.setMemberId(user.getUserId());
					memberFollowListResponse.setMemberRole("user");
					memberFollowListResponse.setMemberName(user.getUserName());
					memberFollowListResponse.setMemberImg(user.getUserProfileImg());
					responseList.add(memberFollowListResponse);
				}

				else {
					Seller seller = sellerRepository.findById(followingMemberId).get();
					memberFollowListResponse.setMemberId(seller.getSellerId());
					memberFollowListResponse.setMemberRole("seller");
					memberFollowListResponse.setMemberName(seller.getSellerName());
					memberFollowListResponse.setMemberImg(seller.getSellerProfileImg());
					responseList.add(memberFollowListResponse);
				}

				memberFollowListResponse.setIsModifiable(false);
				if(followingMemberId.equals(loginMemberId)){
					memberFollowListResponse.setIsModifiable(true);
				}

				memberFollowListResponse.setFollowStatus(false);
				Optional<Following> followingStatus = followingRepository.findByFollowingMemberIdAndFollowerMemberId(loginMemberId, followingMemberId);
				if(followingStatus.isPresent()){
					memberFollowListResponse.setFollowStatus(true);
				}
			}

			memberFollowResult.setMemberFollowListResponseList(responseList);
			return memberFollowResult;
		}

		for (Following following : followerList.subList(startIndex, startIndex+pageContentNumber)) {
			Long followingMemberId = following.getFollowingMemberId();
			String followingMemberRole = following.getFollowingMemberRole();

			MemberFollowListResponse memberFollowListResponse = new MemberFollowListResponse();
			if(followingMemberRole.equals("user")){
				User user = userRepository.findById(followingMemberId).get();
				memberFollowListResponse.setMemberId(user.getUserId());
				memberFollowListResponse.setMemberRole("user");
				memberFollowListResponse.setMemberName(user.getUserName());
				memberFollowListResponse.setMemberImg(user.getUserProfileImg());
				responseList.add(memberFollowListResponse);
			}

			else {
				Seller seller = sellerRepository.findById(followingMemberId).get();
				memberFollowListResponse.setMemberId(seller.getSellerId());
				memberFollowListResponse.setMemberRole("seller");
				memberFollowListResponse.setMemberName(seller.getSellerName());
				memberFollowListResponse.setMemberImg(seller.getSellerProfileImg());
				responseList.add(memberFollowListResponse);
			}

			memberFollowListResponse.setIsModifiable(false);
			if(followingMemberId.equals(loginMemberId)){
				memberFollowListResponse.setIsModifiable(true);
			}

			memberFollowListResponse.setFollowStatus(false);
			Optional<Following> followingStatus = followingRepository.findByFollowingMemberIdAndFollowerMemberId(loginMemberId, followingMemberId);
			if(followingStatus.isPresent()){
				memberFollowListResponse.setFollowStatus(true);
			}
		}

		memberFollowResult.setMemberFollowListResponseList(responseList);
		return memberFollowResult;
	}

	public MemberFollowResult getResponseForFollowing(int size, int startIndex, List<Following> followingList, Long loginMemberId){
		MemberFollowResult memberFollowResult = new MemberFollowResult();
		List<MemberFollowListResponse> responseList = new ArrayList<>();
		if(size < startIndex){
			memberFollowResult.setMemberFollowListResponseList(responseList);
			return memberFollowResult;
		}

		if(size < startIndex + pageContentNumber) {
			for (Following following : followingList.subList(startIndex, size)) {
				Long followerMemberId = following.getFollowerMemberId();
				String followerMemberRole = following.getFollowerMemberRole();

				MemberFollowListResponse memberFollowListResponse = new MemberFollowListResponse();
				if(followerMemberRole.equals("user")){
					User user = userRepository.findById(followerMemberId).get();
					memberFollowListResponse.setMemberId(user.getUserId());
					memberFollowListResponse.setMemberRole("user");
					memberFollowListResponse.setMemberName(user.getUserName());
					memberFollowListResponse.setMemberImg(user.getUserProfileImg());
					responseList.add(memberFollowListResponse);
				}

				else {
					Seller seller = sellerRepository.findById(followerMemberId).get();
					memberFollowListResponse.setMemberId(seller.getSellerId());
					memberFollowListResponse.setMemberRole("seller");
					memberFollowListResponse.setMemberName(seller.getSellerName());
					memberFollowListResponse.setMemberImg(seller.getSellerProfileImg());
					responseList.add(memberFollowListResponse);
				}

				memberFollowListResponse.setIsModifiable(false);
				if(followerMemberId.equals(loginMemberId)){
					memberFollowListResponse.setIsModifiable(true);
				}

				memberFollowListResponse.setFollowStatus(false);
				Optional<Following> followingStatus = followingRepository.findByFollowingMemberIdAndFollowerMemberId(loginMemberId, followerMemberId);
				if(followingStatus.isPresent()){
					memberFollowListResponse.setFollowStatus(true);
				}
			}

			memberFollowResult.setMemberFollowListResponseList(responseList);
			return memberFollowResult;
		}

		for (Following following : followingList.subList(startIndex, startIndex+pageContentNumber)) {
			Long followerMemberId = following.getFollowerMemberId();
			String followerMemberRole = following.getFollowerMemberRole();

			MemberFollowListResponse memberFollowListResponse = new MemberFollowListResponse();
			if(followerMemberRole.equals("user")){
				User user = userRepository.findById(followerMemberId).get();
				memberFollowListResponse.setMemberId(user.getUserId());
				memberFollowListResponse.setMemberRole("user");
				memberFollowListResponse.setMemberName(user.getUserName());
				memberFollowListResponse.setMemberImg(user.getUserProfileImg());
				responseList.add(memberFollowListResponse);
			}

			else {
				Seller seller = sellerRepository.findById(followerMemberId).get();
				memberFollowListResponse.setMemberId(seller.getSellerId());
				memberFollowListResponse.setMemberRole("seller");
				memberFollowListResponse.setMemberName(seller.getSellerName());
				memberFollowListResponse.setMemberImg(seller.getSellerProfileImg());
				responseList.add(memberFollowListResponse);
			}

			memberFollowListResponse.setIsModifiable(false);
			if(followerMemberId.equals(loginMemberId)){
				memberFollowListResponse.setIsModifiable(true);
			}

			memberFollowListResponse.setFollowStatus(false);
			Optional<Following> followingStatus = followingRepository.findByFollowingMemberIdAndFollowerMemberId(loginMemberId, followerMemberId);
			if(followingStatus.isPresent()){
				memberFollowListResponse.setFollowStatus(true);
			}
		}

		memberFollowResult.setMemberFollowListResponseList(responseList);
		return memberFollowResult;
	}
}
