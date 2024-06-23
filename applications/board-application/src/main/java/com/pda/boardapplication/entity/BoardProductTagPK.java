package com.pda.boardapplication.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
@EqualsAndHashCode
public class BoardProductTagPK {

    @ManyToOne
    @JoinColumn(name = "board_id")
    private Board board;

    @Column(name = "product_id")
    private long productId;
}