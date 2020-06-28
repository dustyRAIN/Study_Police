from django.shortcuts import render
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework.parsers import JSONParser, MultiPartParser, FormParser, FileUploadParser
from rest_framework import status
from .serializers import RegisterFaceSerializer
from .models import Face, TestFace
from .utils import crop_save_face, match_face, is_face_available, delete_face
import os

class RegisterFaceView(APIView):
    

    parser_classes = [MultiPartParser, JSONParser, FormParser, FileUploadParser]

    def post(self, request):

        '''
        This method at first extracs the facial information from the uploaded image data.
        Asserts that the image contains a clear face and that face doesn't match with any existing face in the database.
        Then it saves the image as the face of the uploader.

        @param request contains 'user' and 'test_image'
        '''

        serializer = RegisterFaceSerializer(data = request.data)

        prev_image = TestFace.objects.filter(user = request.data['user'])

        for img in prev_image:
            path = './media/TestFaces' 
            filename = img.filename()
            if os.path.exists(os.path.join(path, filename)):
                os.remove(os.path.join(path, filename))
            img.delete()

        if serializer.is_valid():
            serializer.save()
            
            user = request.data['user']
            uploaded_image = TestFace.objects.get(user = user)

            if match_face(uploaded_image.filename(), user):
                return Response({"error": "Person already exists."}, status = status.HTTP_200_OK)

            if crop_save_face(self, uploaded_image.filename(), user):
                return Response(serializer.data, status = status.HTTP_200_OK)
            else:
                return Response({"error": "Face can not be detected."}, status = status.HTTP_400_BAD_REQUEST)
        else:
            return Response({"error": "Bad request or connection error."}, status = status.HTTP_400_BAD_REQUEST)



class MatchFaceView(APIView):
    parser_classes = [MultiPartParser, JSONParser, FormParser, FileUploadParser]

    def post(self, request):

        '''
        This method at first extracs the facial information from the uploaded image data.
        Checks if the uploaded face matches with any existing face in the database.

        @param request contains 'user' and 'test_image'
        '''

        serializer = RegisterFaceSerializer(data = request.data)

        prev_image = TestFace.objects.filter(user = request.data['user'])

        for img in prev_image:
            path = './media/TestFaces' 
            filename = img.filename()
            if os.path.exists(os.path.join(path, filename)):
                os.remove(os.path.join(path, filename))
            img.delete()

        if serializer.is_valid():
            serializer.save()
            
            user = request.data['user']
            uploaded_image = TestFace.objects.get(user = user)
            if match_face(uploaded_image.filename(), user):
                return Response({"matched" : 1}, status = status.HTTP_200_OK)
            else:
                return Response({"matched" : 0}, status = status.HTTP_200_OK)
        else:
            return Response({"matched" : 0}, status = status.HTTP_400_BAD_REQUEST)


class HasFaceView(APIView):

    def get(self, request):

        '''
        This method checks if a user has his/her face image stored in the database

        @param request contains 'user'
        '''

        user = request.data['user']

        if user is None:
            return Response({"exists": -1}, status = status.HTTP_200_OK)

        if is_face_available(user):
            return Response({"exists": 1}, status = status.HTTP_200_OK)
        else:
            return Response({"exists": 0}, status = status.HTTP_200_OK)

            

class DeleteFaceView(APIView):

    def get(self, request):

        '''
        This method deletes face image of a user from the database

        @param request contains 'user'
        '''

        user = request.data['user']

        if delete_face(user):
            return Response({"exists": 0}, status=status.HTTP_200_OK)
        else:
            return Response({"exists": 1}, status = status.HTTP_200_OK)

