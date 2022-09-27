package com.team6.onandthefarm.service.seller;

import com.team6.onandthefarm.dto.seller.SellerDto;
import com.team6.onandthefarm.dto.seller.SellerQnaDto;
import com.team6.onandthefarm.entity.product.ProductQna;
import com.team6.onandthefarm.entity.product.ProductQnaAnswer;
import com.team6.onandthefarm.entity.seller.Seller;
import com.team6.onandthefarm.repository.product.ProductQnaAnswerRepository;
import com.team6.onandthefarm.repository.product.ProductQnaRepository;
import com.team6.onandthefarm.repository.seller.SellerRepository;
import com.team6.onandthefarm.util.DateUtils;
import com.team6.onandthefarm.vo.seller.SellerInfoResponse;
import com.team6.onandthefarm.vo.seller.SellerProductQnaResponse;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SellerServiceImp implements SellerService{

    private SellerRepository sellerRepository;

    private ProductQnaRepository productQnaRepository;

    private ProductQnaAnswerRepository productQnaAnswerRepository;

    private DateUtils dateUtils;

    private Environment env;


    @Autowired
    public SellerServiceImp(SellerRepository sellerRepository,
                            DateUtils dateUtils,
                            Environment env,
                            ProductQnaRepository productQnaRepository,
                            ProductQnaAnswerRepository productQnaAnswerRepository) {
        this.sellerRepository = sellerRepository;
        this.dateUtils=dateUtils;
        this.env=env;
        this.productQnaRepository=productQnaRepository;
        this.productQnaAnswerRepository=productQnaAnswerRepository;
    }

    public boolean updateByUserId(Long userId,SellerDto sellerDto){
        Optional<Seller> sellerEntity = sellerRepository.findById(userId);
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        sellerEntity.get().setSellerZipcode(sellerDto.getZipcode());
        sellerEntity.get().setSellerAddress(sellerDto.getAddress());
        sellerEntity.get().setSellerAddressDetail(sellerDto.getAddressDetail());
        sellerEntity.get().setSellerShopName(sellerDto.getShopName());
        sellerEntity.get().setSellerPassword(sellerDto.getPassword());
        sellerEntity.get().setSellerPhone(sellerDto.getPhone());
        return true;
    }

    /**
     * 셀러 유저의 정보를 조회하는 메서드
     * @param userId
     * @return
     */
    public SellerInfoResponse findByUserId(Long userId){
        Optional<Seller> sellerEntity = sellerRepository.findById(userId);
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        SellerInfoResponse response = modelMapper.map(sellerEntity.get(),SellerInfoResponse.class);
        return response;
    }

    /**
     * 회원의 비밀번호값을 변경해주는 메서드
     * @param sellerDto
     */
    public void updatePassword(SellerDto sellerDto){
        Seller seller = sellerRepository.findBySellerEmail(sellerDto.getEmail());
        seller.setSellerPassword(sellerDto.getPassword());
    }

    /**
     *
     * @param sellerDto
     * @return true: 회원가입 됨  false: 회원가입 실패
     */
    public boolean sellerSignup(SellerDto sellerDto){
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        Seller seller = modelMapper.map(sellerDto, Seller.class);
        String date = dateUtils.transDate(env.getProperty("dateutils.format"));
        seller.setSellerRegisterDate(date);
        sellerRepository.save(seller);
        return true;
    }

    /**
     *
     * @param sellerEmail
     * @return true: 중복안됨 / false: 중복됨
     */
    public boolean sellerIdCheck(String sellerEmail){
        Seller email = sellerRepository.findBySellerEmail(sellerEmail);
        if(email == null){
            return true;
        }
        return false;
    }

    /**
     * 셀러가 가진 QNA 조회
     * @param sellerId
     * @return
     */
    public List<SellerProductQnaResponse> findSellerQnA(Long sellerId){
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        Optional<Seller> seller = sellerRepository.findById(sellerId);
        List<ProductQna> productQnas = productQnaRepository.findBySeller(seller.get());
        List<SellerProductQnaResponse> sellerProductQnaResponses = new ArrayList<>();
        for(ProductQna productQna : productQnas){
            SellerProductQnaResponse response
                    = modelMapper.map(productQna,SellerProductQnaResponse.class);
            sellerProductQnaResponses.add(response);
        }
        return sellerProductQnaResponses;
    }

    /**
     * 답변 생성하는 메서드
     * status
     * qna0 : 답변 대기
     * qna1 : 답변 완료
     * qna2 : qna 삭제
     * @param sellerQnaDto
     */
    public Boolean createQnaAnswer(SellerQnaDto sellerQnaDto){
        Optional<ProductQna> qna = productQnaRepository.findById(Long.valueOf(sellerQnaDto.getProductQnaId()));
        qna.get().setProductQnaStatus("qna1");
        ProductQnaAnswer productQnaAnswer = ProductQnaAnswer.builder()
                .productQna(qna.get())
                .productQnaAnswerContent(sellerQnaDto.getProductQnaAnswerContent())
                .productQnaAnswerCreatedAt(dateUtils.transDate(env.getProperty("dateutils.format")))
                .build();
        ProductQnaAnswer qnaAnswer = productQnaAnswerRepository.save(productQnaAnswer);
        if(qnaAnswer==null){
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }
}