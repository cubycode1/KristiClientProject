package com.domain.gems.commentmodule;

public class CommentsModel {

    private String comment;
    private String commentStatus;

    public CommentsModel(String comment, String commentStatus) {
        this.comment = comment;
        this.commentStatus = commentStatus;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCommentStatus() {
        return commentStatus;
    }

    public void setCommentStatus(String commentStatus) {
        this.commentStatus = commentStatus;
    }
}
