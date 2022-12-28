package com.cos.blog.test;

import com.cos.blog.model.RoleType;
import com.cos.blog.model.User;
import com.cos.blog.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.function.Supplier;

//html파일이 아니라 data를 리턴해주는 controller
@RestController
public class DummyControllerTest {

    @Autowired//의존성 주입(DI)
    private UserRepository userRepository;

    //save 함수는 id를 전달하지 않으면 insert를 해주고
    //save 함수는 id를 전달하면 해당 id에 대한 데이터가 있으면 update를 해주고
    //save 함수는 id를 전달하면 해당 id에 대한 데이터가 없으면 insert를 한다.
    //email, password 수정
    @Transactional//userRepository.save(user); 기능 대체 save 없어도 update가 된다. 함수 종료시 자동 commit이 된다.
    @PutMapping("/dummy/user/{id}")
    public User updateUser(@PathVariable int id, //User requestUser  이렇게 받으면 폼태크 형식으로만 받음
    @RequestBody User requestUser ) //json 데이터를 요청 => Java Object(MessageConverter의 Jackson라이브러리가 변환해서 받아줘요.)
    {
    System.out.println("id:"+id);
    System.out.println("password:"+requestUser.getPassword());
    System.out.println("email:"+requestUser.getEmail());

    User user = userRepository.findById(id).orElseThrow(() ->{
        return new IllegalArgumentException("수정에 실패하였습니다");
    });
    user.setPassword(requestUser.getPassword());
    user.setEmail(requestUser.getEmail());

    //userRepository.save(user);

    //더티체킹 @Transactional를 save 대신 사용 하는 방식
    return user;
    }

    @DeleteMapping("/dummy/user/{id}")
    public String delete(@PathVariable int id) {
        try {
            userRepository.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            return "삭제에 실패하였습니다. 해당 id는 DB에 없습니다.";
        }

        return "삭제 되었습니다. id:"+id;

    }



    //http://localhost:8000/blog/dummy/user/
    @GetMapping("/dummy/users")
    public List<User> list(){
        return userRepository.findAll();
    }

    //한페이지당 2건에 데이터를 리턴 받아 볼 예정
    @GetMapping("/dummy/user")
    public List<User> pageList(@PageableDefault(size=2,sort="id", direction = Sort.Direction.DESC) Pageable pageable){
        Page<User> pagingUser = userRepository.findAll(pageable);
        List<User> users = pagingUser.getContent();
        return users;
    }


    //{id}주소로 파라미터 전달 받을 수 있음
    //http://localhost:8000/blog/dummy/user/3
    @GetMapping("/dummy/user/{id}")
    public User detail(@PathVariable int id) {
        //만약 user/4를 찾으면 (지금 데이터 베이스에 3개만 들어가 있음) 내가 데이터베이스에서 못 찾아오게 되면 user이
        //null이 될것이다. 그럼 리턴 값이 null이 되서 프로그램에 문제가 발생.
        // Optional로 너의 User 객체를 감싸서 가져올테니 null인지 아닌지 판단해서 리턴해줘
        //이건 오류창이 null값이 출력
        /*User user = userRepository.findById(id).orElseGet(new Supplier<User>() {
            @Override
            public User get() {
                return new User();
            }
        });
        return user;*/
        //람다식
       /* User user = userRepository.findById(id).orElseThrow(() -> {
            return new IllegalArgumentException("해당 사용자는 없습니다.");
        });
        return user;*/

        User user = userRepository.findById(id).orElseThrow(new Supplier<IllegalArgumentException>() {
            @Override
            public IllegalArgumentException get() {
                return new IllegalArgumentException("해당 유저는 없습니다. id :" +id);
            }
        });
        //요청 : 웹브라우져
        //user 객체는 = 자바 오브젝트
        //그래서 변환(웹 브라우저가 이해할 수 있는 데이터) -> Json(Gson 라이브러리 사용(예전에)
        //스프링 부트 = MessageConverter 라는 애가 응답시 자동으로 작동
        //만약 자바 오브젝트를 리턴하게 되면 MessageConverter 가 Jackson 라이브러리를 호출해서
        //user 오브젝트를 josn으로 변환해서 브라우저에게 던져준다
        return user;
    }


    //http://localhost:8000/blog/dummy/join(요청)
    //http의 body에 username, password, email 데이터를 가지고 (요청)
    @PostMapping("/dummy/join")
    public String join(User user) {
        System.out.println("username :" + user.getUsername());
        System.out.println("password :" + user.getPassword());
        System.out.println("email :" + user.getEmail());


        user.setRole(RoleType.USER);
        userRepository.save(user);
        return "회원가입이 완료되었습니다.";
    }
}
