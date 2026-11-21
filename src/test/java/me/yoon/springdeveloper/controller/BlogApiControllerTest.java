package me.yoon.springdeveloper.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.yoon.springdeveloper.dao.Article;
import me.yoon.springdeveloper.dto.ArticleCreationRequest;
import me.yoon.springdeveloper.dto.ArticleUpdateRequest;
import me.yoon.springdeveloper.repository.BlogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;
import java.util.List;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;



@SpringBootTest
@AutoConfigureMockMvc
class BlogApiControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    BlogRepository blogRepository;

    @BeforeEach
    void setUp() {
        blogRepository.deleteAll();
    }

    // 여기에 CRUD 테스트 5개(addArticle, findAllArticles, findArticle, deleteArticle, updateArticle)

    @DisplayName("addArticle: 블로그 글 추가 검증")
    @Test
    public void addArticle() throws Exception {

        // given : 저장할 글의 제목과 내용, 요청 dto를 준비하고 JSON문자열로 변환한다.
        final String url = "/api/articles";
        final String title = "title";
        final String content = "content";
        final ArticleCreationRequest userRequest = new ArticleCreationRequest(title, content);

        final String requestBody = objectMapper.writeValueAsString(userRequest);

        // when : POST /api/articles로 요청 본문을 담아 MockMvc 호출을 보낸다.
        ResultActions result = mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(requestBody));

        // then : 201 Created 상태 코드인지 확인하고, DB에 글이 1건 저장됐는지와 제목, 내용이 일치하는지 검증한다.
        result.andExpect(status().isCreated());

        List<Article> articles = blogRepository.findAll();

        assertThat(articles.size()).isEqualTo(1);
        assertThat(articles.get(0).getTitle()).isEqualTo(title);
        assertThat(articles.get(0).getContent()).isEqualTo(content);
    }

    @DisplayName("findArticle: 블로그 글 조회 검증")
    @Test
    public void findArticle() throws Exception {

        // given : 조회할 테스트용 글 1개를 blogRepository.save()로 미리 저장한다.
        final String url = "/api/articles/{id}";
        final String title = "title";
        final String content = "content";

        Article savedArticle = blogRepository.save(Article.builder()
                .title(title)
                .content(content)
                .build());

        // when : 저장된 글의 id(savedArticle.getId())로 get /api/articles/{id} 요청을 보낸다.
        final ResultActions resultActions = mockMvc.perform(get(url, savedArticle.getId()));

        // then : 200 Ok 응답과 함께 JSON응답의 title, content가 저장한 값과 같은지 확인한다.
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value(content))
                .andExpect(jsonPath("$.title").value(title));
    }

    @DisplayName("updateArticle: 블로그 글 수정 검증")
    @Test
    public void updateArticle() throws Exception {

        // given : 기존 글 하나를 먼저 저장하고, 수정할 새 제목, 내용을 담은 ArticleUpdateRequest 객체를 준비한다.
        final String url = "/api/articles/{id}";
        final String title = "title";
        final String content = "content";

        Article savedArticle = blogRepository.save(Article.builder()
                .title(title)
                .content(content)
                .build());

        final String newTitle = "new title";
        final String newContent = "new content";

        ArticleUpdateRequest request = new ArticleUpdateRequest(newTitle, newContent);

        // when : 저장된 글의 id로 PUT /api/articles/{id} 요청을 보내며 수정 DTO를 JSON으로 전달한다.
        ResultActions result = mockMvc.perform(put(url, savedArticle.getId())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request)));

        // then : 200 OK를 확인한 뒤, findById()로 다시 조회해 제목과 내용이 새 값으로 바뀌었는지 검증한다.
        result.andExpect(status().isOk());

        Article article = blogRepository.findById(savedArticle.getId()).get();

        assertThat(article.getTitle()).isEqualTo(newTitle);
        assertThat(article.getContent()).isEqualTo(newContent);
    }

    @DisplayName("deleteArticle: 블로그 글 삭제 검증")
    @Test
    public void deleteArticle() throws Exception {
        // given : 삭제할 테스트용 글 1개를 blogRepository.save()로 저장한다.
        final String url = "/api/articles/{id}";
        final String title = "title";
        final String content = "content";

        Article savedArticle = blogRepository.save(Article.builder()
                .title(title)
                .content(content)
                .build());

        // when : 저장된 글의 id로 Delete /api/articles/{id} 요청을 보낸다.
        mockMvc.perform(delete(url, savedArticle.getId()))
                .andExpect(status().isOk());

        // then : 200 ok를 확인하고, blogRepository.findAll() 결과가 비었는지 검증한다.
        List<Article> articles = blogRepository.findAll();

        assertThat(articles).isEmpty();
    }


    @DisplayName("findAllArticles: 블로그 글 목록 조회 검증")
    @Test
    public void findAllArticles() throws Exception {
        // given : 서로 다른 제목과 내용의 글 2개를 blogRepository.save()로 미리 저장한다.
        final String url = "/api/articles";
        final String title1 = "title1";
        final String content1 = "content1";
        final String title2 = "title2";
        final String content2 = "content2";

        blogRepository.save(Article.builder()
                .title(title1)
                .content(content1)
                .build());

        blogRepository.save(Article.builder()
                .title(title2)
                .content(content2)
                .build());

        // when : GET /api/articles로 목록 조회 요청을 보낸다.
        final ResultActions resultActions = mockMvc.perform(get(url)
                .accept(MediaType.APPLICATION_JSON));

        // then : 200 Ok와 응답 배열길이가 2인지 확인하고 0번, 1번 요소의 title과 content가 저장한 값과 일치하는지 검증한다.
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value(title1))
                .andExpect(jsonPath("$[0].content").value(content1))
                .andExpect(jsonPath("$[1].title").value(title2))
                .andExpect(jsonPath("$[1].content").value(content2));
    }
}