from django.shortcuts import render
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework.parsers import JSONParser, MultiPartParser, FormParser, FileUploadParser
from rest_framework import status
from .models import GeneralNotification
from .serializers import PostNotificationSerializer, GetNotificationSerializer
from users.models import CustomUser
from materials.models import Materials
from classes.models import ClassHasStudent
from django.db.models.functions import Coalesce
import datetime



def post_notification(classroom, noti_type):

    '''
    This function sends a specific type of notification to all the students in a sprecific classroom.
    @param classroom, @param noti_type
    '''

    students = ClassHasStudent.objects.filter(classroom = classroom)
    current_utc_milli = datetime.datetime.now().timestamp()
    print(current_utc_milli)
    current_utc_milli = int(current_utc_milli * 1000)
    if students is not None:
        for stu in students:
            stu_id = stu.getStudentID()
            serializer = PostNotificationSerializer(data={"classroom" : classroom , "user": stu_id, "seen" : 0, "noti_type": noti_type, "time": current_utc_milli})
            if serializer.is_valid():
                serializer.save()



class GetNotificationView(APIView):

    '''
    Defines get method retrieve  all the notification of a certain user.
    '''

    def get(self, request):

        '''
        This method returns info about all the notifications for a specific user.
        
        @param request should contain 'user'
        '''

        user = request.data['user']

        if user is not None:
            results = GeneralNotification.objects.filter(user = user).order_by('seen', '-time')

        serializer = GetNotificationSerializer(results, many = True)

        return Response(serializer.data, status=status.HTTP_200_OK)



class NotificationSeenView(APIView):

    '''
    Defines post method to change the seen status of a notification.
    '''

    def post(self, request):

        '''
        This method changes the seen status of a notification to 1 (the notification is seen)

        @param request should contain 'id'
        '''

        noti_id = request.data['id']

        if noti_id is not None:
            noti = GeneralNotification.objects.get(id = noti_id)
            if noti is not None:
                noti.seen = 1
                noti.save()
                return Response({"details": "ok"}, status=status.HTTP_200_OK)
            
            return Response({"details": "bad request"}, status=status.HTTP_400_BAD_REQUEST)

        return Response({"details": "bad request"}, status=status.HTTP_400_BAD_REQUEST)
