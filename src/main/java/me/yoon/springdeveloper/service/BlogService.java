package me.yoon.springdeveloper.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import me.yoon.springdeveloper.dao.Article;
import me.yoon.springdeveloper.dto.ArticleCreationRequest;
import me.yoon.springdeveloper.dto.ArticleUpdateRequest;
import me.yoon.springdeveloper.repository.BlogRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class BlogService {
    private final BlogRepository blogRepository;

    public Article save(ArticleCreationRequest request) {
        return blogRepository.save(request.toEntity());
    }

    public List<Article> findAll() {
        return blogRepository.findAll();
    }

    public Article findbyid(long id) {
        return blogRepository.findById(id).orElse(null);
    }

    public void deleteById(long id) {
        blogRepository.deleteById(id);
    }

    @Transactional
    public Article updateArticle(Long id, ArticleUpdateRequest req) {
        Article article = blogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        article.update(req.getTitle(), req.getContent());

        return article;
    }
}