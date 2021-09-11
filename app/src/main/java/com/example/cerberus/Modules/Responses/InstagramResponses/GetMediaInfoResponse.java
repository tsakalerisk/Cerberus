package com.example.cerberus.Modules.Responses.InstagramResponses;

public class GetMediaInfoResponse {
    public GraphQL graphql;

    public class GraphQL {
        public ShortcodeMedia shortcode_media;

        public class ShortcodeMedia {
            public User owner;
        }
    }
}
