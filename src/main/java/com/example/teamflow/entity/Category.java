package com.example.teamflow.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
public class Category extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty(message = "カテゴリー名を入力してください")
    @Column(name = "category_name")
    private String categoryName;
}

//categories
//- id (PK)
//- カテゴリー名
//以下自動生成
//- 作成日時
//- 最終更新日時
//- 最終更新者
//- 削除ステータス