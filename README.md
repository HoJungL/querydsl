# Querydsl
- 버전 이슈가 상당히 많기 때문에, 그 버전에 맞춰서 써야함!
- 3.0이상 버전의 경우, 굳이 plugin을 하지 말고, 내장되어있는 코드를 사용하기


### @BeforeEach
- 테스트  실행전, 데이터를 미리 세팅함.


## JPQL vs Querydsl
- jpql의 경우 setparameter를 해줘야함
- querydsl은 setparameter를 안해줘도 됌!(eq)만 뜨면 됨.
- querydsl은 SQL injection 공격에 유리함.
- querydsl은 compile 시점에서 에러가 잡임

## Q-Type 활용
- 생성된게 있으면 그냥 쓰면됨
- static import를 해서 변경 가능.

## 검색 조건 쿼리
- JPQL이 제공하는 모든 검색 조건 제공
- 검색시 where 절에 eq(=), ne(!=), isNotNull, in, notin,
- between, goe(>=), gt(>), loe(<=), lt(<),
- like, contains(%member%), startsWith(member%)등 걸수 잇음.

## 결과 조회 쿼리
- Querydsl은 향후 fetchCount() , fetchResult() 를 지원하지 않기로 결정
- fetchResults는 결국 카운트와 관련된 코드 이기 떄문에, 아래와 같이 코드를 써야함.

```
  public void count() {
  Long totalCount = queryFactory
  //.select(Wildcard.count) //select count(*)
    .select(member.count()) //select count(member.id)
    .from(member)
    .fetchOne();
    System.out.println("totalCount = " + totalCount);
}
```

## 정렬, 페이징
```     
* 회원 정렬 순서
* 1. 회원 나이 내림차순
* 2. 회원 이름 올림차순
* 단, 2에서 회원 이름이 없으면 마지막에 출력(nulls last)
```
```
List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.age.desc(), member.username.asc().nullsLast())
                .fetch();
}
```

## 집합
- 튜플형태로 들어가기 때문에 get을 써야함.
```
    public void aggregation() {
        List<Tuple> result = queryFactory
                .select(
                        member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min()
                )
                .from(member)
                .fetch();

        Tuple tuple = result.get(0);
}
```

## 기본 조인
- 기본 조인은 "inner join"이 실행됨.
- extracting : 특정 열
- containsExactly : 순서를 포함해서 정확히 일치해야함
- 세타조인 : 외부 조인 불가능!!! 대신 on을 쓰면 외부 조인 가능.

## 조인 - on절(1)
- on 절을 사용해서 filtering 할때, inner join을 하면 where 절에서 필터링 하는 것과 동일!
- 따라서, 내부조인이면 where 쓰고, 외부 join이 필수다! 하면 on을 쓰는걸로!

## 조인 -on절(2)
- 연관관계가 없는 entity간의 외부 조인
- 이때는, select에서 entity를 다 써주고 -> leftjoin에 넣을 entity를 바로 쓴 뒤에, on으로 eq 작성

## 조인 - fetch join
- SQL 조인을 활용해, 연관된 entity를 SQL 한번에 조회하는 기능으로, 주로 성능 최적화에 사용하는 방법
```angular2html
queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"))
                .fetchOne();

->
queryFactory
                .selectFrom(member)
                .join(member.team, team).fetchJoin() // 요게 들어감
                .where(member.username.eq("member1"))
                .fetchOne();
```

## 서브 쿼리
- 서브 쿼리기 때문에, 안의 member가 겹치면 안됌!!!
- 한계점
```
- JPA JPQL 서브쿼리는 from 절의 서브쿼리(인라인 뷰) 지원 안함.
- Querydsl도 지원 안됨..
- 하이버 네이트 구현체(JPAExpressions)를 사용하면 select절의 서브쿼리는 지원함.
```
- from 절의 서브쿼리 해결방안
```
1. 서브 쿼리를 join으로 변경함.(될수도 안될수도)
2. 앱에서 쿼리를 2번 분리해서 실행함.(성능이 좋아야할 경우에는... 안됨)
3. nativeSQL을 사용해야함...
```

## Case 문
- select, 조건절(where), order by에서 사용 가능
- 복잡한 Case 문에서는 CaseBuilder()라는 걸 써야한다고 함.

## 상수, 문자 더하기
- 문자 + 상수는 입력되지 않기때문에, ~.stringValue()를 써줘야 concat이 됩니다.
- stringValue() : 문자가 아닌 다른 타입들을 문자로 변환할수 있는 방법으로, ENUM을 처리할때 자주 사용함.



# 중급 문법

## 프로젝션과 결과 반환 - 기본
- 프로젝션
- DTO: bean을 만들고, 여러 데이터를 받을 수 있도록 자료구조를 만들어 놓은것.

## 프로젝션과 결과 반환 - DTO 조회
- 순수 JPA에서 DTO로 조회할때는 em.cQ("select new path(DTO내용)",Dto.class) 형식으로 써줘야함.
- 생성자 방식만 지원함 --> setter, 필드에 바로 값 주입 안됨 (public Dto 있어야한다는 말)

- Querydsl 빈 생성(Bean population)
- 프로퍼티 접근(Setter), 필드 직접 접근, 생성자 사용

```
    @Test
    public void findUserDto() {
        QMember memberSub = new QMember("memberSub");
        List<UserDto> result = queryFactory
                .select(Projections.fields(UserDto.class,
                        member.username.as("name"),
                        
                        //서브 쿼리 부분 age에 매칭하기.
                        ExpressionUtils.as(JPAExpressions
                                .select(memberSub.age.max())
                                .from(memberSub), "age")
                ))
                .from(member)
                .fetch();
```

## 프로젝션과 결과반환 - @QueryProjection
- DTO에 @QueryProjection을 써주기만 하면 됌.
- 의존성을 띔. ㅠㅠ

## 동적 쿼리(1) -BooleanBuilder 사용
```
    @Test
    public void dynamicQuery_BooleanBuilder() {
        String usernameParam = "member1";
        Integer ageParam = 10;

        List<Member> result = searchMember1(usernameParam, ageParam);
        assertThat(result.size()).isEqualTo(1);
    }

    private List<Member> searchMember1(String usernameCond, Integer ageCond) {

        BooleanBuilder builder = new BooleanBuilder();
        if (usernameCond != null) {
            builder.and(member.username.eq(usernameCond));
        }

        // null이면 이쪽 코드를 안탐. where 문에 관여 자체를 안함.
        if (ageCond != null) {
            builder.and(member.age.eq(ageCond));
        }

        return queryFactory
                .selectFrom(member)
                .where(builder)
                .fetch();
    }
```


## 동적 쿼리(2) - where 다중 파라미터 사용
- method를 다른 쿼리에서 재사용 가능
- 가독성이 높아짐.
- - 조합이 가능해짐.
```
    private BooleanExpression allEq(String usernameCond, Integer ageCond) {
        return usernameEq(usernameCond).and(ageEq(ageCond));
    }
```


## 수정, 삭제 벌크 연산
- 쿼리 한번으로 대량 데이터를 수정할때 쓰는 방법.
- 벌크 연산은 DB를 바로 변경시켜버림.
- JPA는 영속성 컨텍스트에 올라가있음. -> DB와 영속성 컨텍스트에 있는 값이 달라짐. 
- 이상태에서 selectFrom().fetch()해버리면... DB보다 영속성 컨텍스트가 우선권을 가져버림
- 따라서, em.flush(), em.clear()를 꼭해야...
- 덧셈은 add, 곱하기는 multiply 하면 됨.


## SQL function 호출하기
- 기본적으로 JPA와 같이 Dialect에 등록된 내용만 호출 가능


# 실무 활용 - 순수 JPA와 Querydsl

## 순수 JPA 리포지토리와 Querydsl
- JPAQueryFactory는 Application에 @Bean으로 등록해도 됨
```
@Bean
JPAQueryFactory jpaQueryFactory(EntityManaget em){
    return new JPAQueryFactory(em);
    }
    -> 이걸 쓰면 대신 RequiredArgsConstructor를 써서 생성자 삭제가능
```

## 동적 쿼리와 성능 최적화 조회(1) - Builder 사용


## 동적 쿼리와 성능 최적화 조회(2) - Where 절 파라미터 사용
- method 재사용이 진짜 진짜 진짜 좋은 장점임..
- BooleanExpression끼리 조합 가능

## 조회 API 컨트롤러 개발
- @GetMapping을 통해서 생성


# 섹션 6 스프링 데이터 JPA와 Querydsl

## 스프링 데이터 JPA 리포지토리로 변경
- 기본적인 crud, 정적쿼리는 스프링 데이터 JPA가 인터페이스를 만들면, 이름 매칭후, 구현체가 꽂아줌.

## 사용자 정의 리포지토리
- 사용자 정의 리포지토리 사용법
- 1. 사용자 정의 인터페이스 작성
- 2. 사용자 정의 인터페이스 구현
- 3. 스프링 데이터 리포지토리에 사용자 정의 인터페이스 상속 
- - Impl로 이름 써야 JPA 쓰기 편함!
- - 특화된 기능(재사용도 가능하다면)이라면 Repository로 하나 만들어서 따로 만드는 것도 괜찮아요.

## 스프링데이터 페이징 활용1 - querydsl 페이징 연동
- 스프링 데이터의 Page, pageable 활용
- 전체 카운트를 한번에 조회하는 단순한 방법
- 데이터 내용과 전체 카운트를 별도로 조회하는 방법 등 가능
```
차이점
단순 조회의 경우, fetchResults를 통해 쿼리DSL이 토탈을 날려줌
별도 조회의 경우, 직접 내가 토탈 쿼리를 날리는 것.
-> count query를 최적화 할 수 잇음.
```
## 스프링데이터 페이징 활용2 - querydsl 최적화
- 스프링 데이터 라이브러리가 제공해줌
- count 쿼리가 생략 가능한 경우 
- - 페이지 시작이면서 컨텐츠 사이즈가 페이지 사이즈보다 작을때
- - 마지막 페이지 일때 (offset + 컨텐츠 사이즈를 더해서 전체 사이즈 구함)
```
// 최적화 전
       long total = queryFactory
                .select(member)
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                )
                .fetchCount();

        return new PageImpl<>(content, pageable, total);
        
      
// 최적화 후
        JPAQuery<Member> countQuery = queryFactory
                .select(member)
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                );

        return PageableExecutionUtils.getPage(content, pageable, () -> countQuery.fetchCount());

```

## 스프링데이터 페이징 활용3 - 컨트롤러 개발

# 섹션 7 스프링 데이터 JPA가 제공하는 Querydsl 기능

## 인터페이스 지원 - QuerydslPredicateExecutor
- QuerydslPredicateExecutor
- - table이 다중으로 생길경우, 못씀..
- leftJoin이 불가능함
- 서비스 클래스가 Querydsl이라는 구현 기술에 의존해야함..!!
- 실무에서는 쓰기 어려움.

## Querydsl Web 지원
- 공식 URL 보기
- 한계점
1. 단순한 조건만 가능(contain, in 등등)
2. 조건을 커스텀 하는 기능이 복잡
3. 컨트롤러가 Querydsl에 의존

## 리포지토리 지원 - QuerydslRepositorySupport
- Impl extends QuerydslRepositorySupport 를 쓰면 됨.
- 난.. 안쓸듯..
## Querydsl 지원 클래스 직접 만들기

